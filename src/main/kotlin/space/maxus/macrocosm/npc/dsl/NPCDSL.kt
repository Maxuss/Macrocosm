package space.maxus.macrocosm.npc.dsl

import org.bukkit.Sound
import space.maxus.macrocosm.npc.ops.*
import kotlin.time.Duration
import kotlin.time.DurationUnit

@DslMarker
annotation class NPCDSL

/**
 * Starts a dialogue with entity
 */
@NPCDSL
inline fun beginDialogue(builder: NPCDialogueBuilder.() -> Unit) = NPCDialogueBuilder(mutableListOf()).apply(builder).dialogueElements

@NPCDSL
class NPCDialogueBuilder(var dialogueElements: MutableList<NPCOp>) {
    /**
     * Says something from the NPC
     */
    @NPCDSL
    fun say(message: String) = dialogueElements.add(NPCOpSay(message))

    /**
     * Waits for certain amount of seconds
     */
    @NPCDSL
    fun wait(seconds: Number) = dialogueElements.add(NPCOpWait(seconds))

    /**
     * Waits for certain duration
     */
    @NPCDSL
    fun wait(duration: Duration) = dialogueElements.add(NPCOpWait(duration.toDouble(DurationUnit.SECONDS)))

    /**
     * Sends a raw message to player
     */
    @NPCDSL
    fun message(message: String) = dialogueElements.add(NPCOpSendMessage(message))

    /**
     * Grants player a goal
     */
    @NPCDSL
    fun goal(goal: String) = dialogueElements.add(NPCOpGrantGoal(goal))

    /**
     * A predicate to check whether the player has achieved goal. Optionally also grants the goal to player
     */
    @NPCDSL
    fun lacksGoal(goal: String, grant: Boolean = true): (NPCOperationData) -> Boolean = {
        val hasGoal = it.player.hasReachedGoal(goal)
        if(!hasGoal) {
            if(grant)
                it.player.reachGoal(goal)
            true
        } else false
    }

    /**
     * Plays a sound at NPC location
     */
    @NPCDSL
    fun playSound(sound: Sound, pitch: Number = 1, volume: Number = 1) = dialogueElements.add(NPCOpPlaySound(sound, pitch, volume))

    /**
     * Branches dialogue logic based on predicate
     */
    @NPCDSL
    fun branch(predicate: (NPCOperationData) -> Boolean): NPCDialogueBranchBuilder = NPCDialogueBranchBuilder(this, predicate, mutableListOf(), mutableListOf())

    @NPCDSL
    class NPCDialogueBranchBuilder(val builder: NPCDialogueBuilder, val predicate: (NPCOperationData) -> Boolean, val trueBranch: MutableList<NPCOp>, val elseBranch: MutableList<NPCOp>) {
        /**
         * Executes dialogue logic if the predicate was successful
         */
        @NPCDSL
        inline infix fun then(branch: NPCDialogueBuilder.() -> Unit): NPCDialogueBranchBuilder {
            val builder = NPCDialogueBuilder(mutableListOf())
            builder.apply(branch)
            this.trueBranch.addAll(builder.dialogueElements)
            return this
        }

        /**
         * Executes dialogue logic if the predicate was unsuccessful
         */
        @NPCDSL
        inline infix fun otherwise(branch: NPCDialogueBuilder.() -> Unit) {
            val builder = NPCDialogueBuilder(mutableListOf())
            builder.apply(branch)
            this.elseBranch.addAll(builder.dialogueElements)
            this.builder.dialogueElements.add(NPCOpBranch(predicate, trueBranch, elseBranch))
        }
    }
}
