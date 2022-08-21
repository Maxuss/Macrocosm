package space.maxus.macrocosm.players.banking

import net.minecraft.nbt.*
import space.maxus.macrocosm.InternalMacrocosmPlugin
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.db.Accessor
import space.maxus.macrocosm.util.recreateFile
import space.maxus.macrocosm.util.runCatchingReporting
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.TimeUnit
import kotlin.io.path.createFile
import kotlin.io.path.exists

class TransactionHistory(val transactions: ConcurrentLinkedDeque<Transaction>) {
    var memoryThreshold: Long = 2L shl 6
    fun remember(transaction: Transaction) {
        if(transactions.size >= memoryThreshold) {
            transactions.pop()
        }
        transactions.add(transaction)
    }

    fun storeSelf() {
        Threading.runAsyncRaw {
            runCatchingReporting {
                val file = Accessor.access("transactions.dat")
                file.recreateFile()
                val tag = CompoundTag()
                val list = ListTag()
                for(transaction in transactions) {
                    val cmp = CompoundTag()
                    cmp.put("Kind", IntTag.valueOf(transaction.kind.ordinal))
                    cmp.put("Amount", ByteArrayTag(transaction.amount.toBigInteger().toByteArray()))
                    val bb = ByteBuffer.allocate(16)
                    bb.putLong(transaction.player.mostSignificantBits)
                    bb.putLong(transaction.player.leastSignificantBits)
                    cmp.put("Initiator", ByteArrayTag(bb.array()))
                    list.add(cmp)
                }
                tag.put("Transactions", list)
                NbtIo.writeCompressed(tag, file.toFile())
            }
        }
    }

    companion object {
        fun readSelf() {
            Threading.runAsyncRaw {
                val pool = Threading.newFixedPool(16)
                runCatchingReporting {
                    val file = Accessor.access("transactions.dat")
                    val history = if(!file.exists()) {
                        file.createFile()
                        TransactionHistory(ConcurrentLinkedDeque())
                    } else {
                        val data = NbtIo.readCompressed(file.toFile())
                        val queue = ConcurrentLinkedDeque<Transaction>()
                        data.getList("Transactions", ListTag.TAG_COMPOUND.toInt()).forEach {
                            pool.execute {
                                val cmp = it as CompoundTag
                                val bb = ByteBuffer.wrap(cmp.getByteArray("Initiator"))
                                val player = UUID(bb.getLong(0), bb.getLong(1))
                                val kind = Transaction.Kind.values()[cmp.getInt("Kind")]
                                val amount = BigInteger(cmp.getByteArray("Amount")).toBigDecimal()
                                queue.push(Transaction(player, kind, amount))
                            }
                        }
                        TransactionHistory(queue)
                    }
                    InternalMacrocosmPlugin.TRANSACTION_HISTORY = history
                }

                pool.shutdown()
                pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
            }
        }
    }
}
