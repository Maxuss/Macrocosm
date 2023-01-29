package space.maxus.macrocosm.npc.dsl

import org.bukkit.Sound
import space.maxus.macrocosm.npc.ops.*
import kotlin.time.Duration
import kotlin.time.DurationUnit

@DslMarker
annotation class NPCDSL

@NPCDSL
inline fun beginDialogue(builder: NPCDialogueBuilder.() -> Unit) = NPCDialogueBuilder(mutableListOf()).apply(builder).dialogueElements

@NPCDSL
class NPCDialogueBuilder(var dialogueElements: MutableList<NPCOp>) {
    @NPCDSL
    fun say(message: String) = dialogueElements.add(NPCOpSay(message))
    @NPCDSL
    fun wait(seconds: Number) = dialogueElements.add(NPCOpWait(seconds))
    @NPCDSL
    fun wait(duration: Duration) = dialogueElements.add(NPCOpWait(duration.toDouble(DurationUnit.SECONDS)))
    @NPCDSL
    fun message(message: String) = dialogueElements.add(NPCOpSendMessage(message))
    @NPCDSL
    fun goal(goal: String) = dialogueElements.add(NPCOpGrantGoal(goal))
    @NPCDSL
    fun lacksGoal(goal: String, grant: Boolean = true): (NPCOperationData) -> Boolean = {
        val hasGoal = it.player.hasReachedGoal(goal)
        if(!hasGoal) {
            if(grant)
                it.player.reachGoal(goal)
            true
        } else false
    }
    @NPCDSL
    fun playSound(sound: Sound, pitch: Number = 1, volume: Number = 1) = dialogueElements.add(NPCOpPlaySound(sound, pitch, volume))
    @NPCDSL
    fun branch(predicate: (NPCOperationData) -> Boolean): NPCDialogueBranchBuilder = NPCDialogueBranchBuilder(this, predicate, mutableListOf(), mutableListOf())

    @NPCDSL
    class NPCDialogueBranchBuilder(val builder: NPCDialogueBuilder, val predicate: (NPCOperationData) -> Boolean, val trueBranch: MutableList<NPCOp>, val elseBranch: MutableList<NPCOp>) {
        @NPCDSL
        inline infix fun then(branch: NPCDialogueBuilder.() -> Unit): NPCDialogueBranchBuilder {
            val builder = NPCDialogueBuilder(mutableListOf())
            builder.apply(branch)
            this.trueBranch.addAll(builder.dialogueElements)
            return this
        }

        @NPCDSL
        inline infix fun otherwise(branch: NPCDialogueBuilder.() -> Unit) {
            val builder = NPCDialogueBuilder(mutableListOf())
            builder.apply(branch)
            this.elseBranch.addAll(builder.dialogueElements)
            this.builder.dialogueElements.add(NPCOpBranch(predicate, trueBranch, elseBranch))
        }
    }
}
