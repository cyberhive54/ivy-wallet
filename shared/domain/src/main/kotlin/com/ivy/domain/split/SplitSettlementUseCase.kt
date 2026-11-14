package com.ivy.domain.split

import com.ivy.data.db.dao.write.WriteSplitSettlementDao
import com.ivy.data.db.entity.SplitSettlementEntity
import com.ivy.data.model.AccountId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.PositiveValue
import com.ivy.data.model.TransactionId
import com.ivy.data.model.TransactionMetadata
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.model.primitive.PositiveDouble
import com.ivy.data.repository.TransactionRepository
import kotlinx.collections.immutable.persistentListOf
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

/** The outcome of applying a settle-up plan. */
data class SettlementResult(
    /** Ids of the settlement rows that were written to the Splitly ledger. */
    val settlementIds: List<UUID>,
    /** Ids of the transactions that were written on the local user's account. */
    val transactionIds: List<UUID>,
)

/**
 * Applies a settle-up plan to the database.
 *
 * Every transfer of the plan produces:
 * - a `split_settlements` row (the Splitly ledger entry), and
 * - a real `Transaction` on the local user's account - but only for the pairs
 *   the local user takes part in. Money that only moves between other members
 *   never touches the local user's account.
 *
 * Both halves are written together: if the ledger row can't be written the
 * already-written transaction is rolled back.
 */
class SplitSettlementUseCase @Inject constructor(
    private val writeSplitSettlementDao: WriteSplitSettlementDao,
    private val transactionRepository: TransactionRepository,
) {

    suspend fun apply(
        groupId: UUID,
        groupName: String,
        currency: String,
        transfers: List<Transfer>,
        memberNames: Map<UUID, String>,
        localMemberId: UUID,
        accountId: UUID,
        assetCode: String,
        now: Instant = Instant.now(),
    ): SettlementResult {
        val settlements = mutableListOf<SplitSettlementEntity>()
        val transactionIds = mutableListOf<UUID>()

        transfers.forEach { transfer ->
            val transactionId = UUID.randomUUID()
            val amount = transfer.amount

            // 1. The real transaction - only for the local user's own money
            val isIncome = transfer.toMemberId == localMemberId
            val isExpense = transfer.fromMemberId == localMemberId
            val converted = if (isIncome || isExpense) {
                GroupLedger.convert(amount, currency, assetCode)
            } else {
                BigDecimal.ZERO
            }
            if ((isIncome || isExpense) && converted > BigDecimal.ZERO) {
                val otherMemberId = if (isIncome) transfer.fromMemberId else transfer.toMemberId
                val otherName = memberNames[otherMemberId] ?: "member"
                transactionRepository.save(
                    buildTransaction(
                        id = transactionId,
                        isIncome = isIncome,
                        amount = converted,
                        assetCode = assetCode,
                        title = "Settle - $otherName",
                        description = "$groupName group settle-up",
                        accountId = accountId,
                        now = now,
                    )
                )
                transactionIds.add(transactionId)
            }

            // 2. The Splitly ledger row
            val settlement = SplitSettlementEntity(
                groupId = groupId,
                fromMemberId = transfer.fromMemberId,
                toMemberId = transfer.toMemberId,
                amount = amount.toDouble(),
                currency = currency,
                createdAt = now,
                linkedTransactionId = transactionId.takeIf { transactionIds.contains(transactionId) },
            )
            try {
                writeSplitSettlementDao.save(settlement)
            } catch (e: Exception) {
                // Roll back the transaction - both halves must land together
                if (isIncome || isExpense) {
                    runCatching { transactionRepository.deleteById(TransactionId(transactionId)) }
                }
                throw e
            }
            settlements.add(settlement)
        }

        return SettlementResult(
            settlementIds = settlements.map { it.id },
            transactionIds = transactionIds,
        )
    }

    private fun buildTransaction(
        id: UUID,
        isIncome: Boolean,
        amount: BigDecimal,
        assetCode: String,
        title: String,
        description: String,
        accountId: UUID,
        now: Instant,
    ) = if (isIncome) {
        Income(
            id = TransactionId(id),
            title = NotBlankTrimmedString.from(title).getOrNull(),
            description = NotBlankTrimmedString.from(description).getOrNull(),
            category = null,
            time = now,
            settled = true,
            metadata = TransactionMetadata(
                recurringRuleId = null,
                paidForDateTime = null,
                loanId = null,
                loanRecordId = null,
            ),
            tags = persistentListOf(),
            value = PositiveValue(
                amount = PositiveDouble.unsafe(amount.toDouble()),
                asset = AssetCode.unsafe(assetCode),
            ),
            account = AccountId(accountId),
        )
    } else {
        Expense(
            id = TransactionId(id),
            title = NotBlankTrimmedString.from(title).getOrNull(),
            description = NotBlankTrimmedString.from(description).getOrNull(),
            category = null,
            time = now,
            settled = true,
            metadata = TransactionMetadata(
                recurringRuleId = null,
                paidForDateTime = null,
                loanId = null,
                loanRecordId = null,
            ),
            tags = persistentListOf(),
            value = PositiveValue(
                amount = PositiveDouble.unsafe(amount.toDouble()),
                asset = AssetCode.unsafe(assetCode),
            ),
            account = AccountId(accountId),
            necessity = null,
        )
    }
}
