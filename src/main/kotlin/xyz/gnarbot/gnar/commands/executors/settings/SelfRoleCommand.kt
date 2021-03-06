package xyz.gnarbot.gnar.commands.executors.settings

import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.IMentionable
import net.dv8tion.jda.core.entities.Role
import xyz.gnarbot.gnar.commands.Category
import xyz.gnarbot.gnar.commands.Command
import xyz.gnarbot.gnar.commands.template.CommandTemplate
import xyz.gnarbot.gnar.commands.template.Executor
import xyz.gnarbot.gnar.utils.Context

@Command(
        id = 59,
        aliases = arrayOf("selfroles", "selfrole"),
        usage = "(add|remove|clear) [@role]",
        description = "Set self-roles that users can assign to themselves.",
        category = Category.SETTINGS,
        permissions = arrayOf(Permission.MANAGE_ROLES)
)
class SelfRoleCommand : CommandTemplate() {
    @Executor(0, description = "Add a self-role.")
    fun add(context: Context, role: Role) {
        if (!context.guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) {
            context.send().error("The bot needs the ${Permission.MANAGE_ROLES.getName()} permission.").queue()
            return
        }

        if (role == context.guild.publicRole) {
            context.send().error("You can't add the public role!").queue()
            return
        }

        if (!context.guild.selfMember.canInteract(role)) {
            context.send().error("That role is higher than my role! Fix by changing the role hierarchy.").queue()
            return
        }

        if (role.id in context.data.roles.selfRoles) {
            context.send().error("${role.asMention} is already added as a self-assignable role.").queue()
            return
        }

        context.data.roles.selfRoles.add(role.id)
        context.data.save()

        context.send().embed("Self-Roles") {
            desc {
                "Added ${role.asMention} to the list of self-assignable roles. Users can get them using `_iam`."
            }
        }.action().queue()
    }

    @Executor(1, description = "Remove a self-role.")
    fun remove(context: Context, role: Role) {
        if (role.id !in context.data.roles.selfRoles) {
            context.send().error("${role.asMention} is not a self-assignable role.").queue()
            return
        }

        context.data.roles.selfRoles.remove(role.id)
        context.data.save()

        context.send().embed("Self-Roles") {
            desc {
                "Removed ${role.asMention} from the list of self-assignable roles."
            }
        }.action().queue()
    }

    @Executor(2, description = "Clear all self-assignable roles.")
    fun clear(context: Context) {
        if (context.data.roles.selfRoles.isEmpty()) {
            context.send().error("This guild doesn't have any self-assignable roles.").queue()
            return
        }

        context.data.roles.selfRoles.clear()
        context.data.save()

        context.send().embed("Self-Roles") {
            desc {
                "Cleared the list of self-assignable roles."
            }
        }.action().queue()
    }

    @Executor(3, description = "List self-assignable roles.")
    fun list(context: Context) {
        context.send().embed("Self-Roles") {
            desc {
                if (context.data.roles.selfRoles.isEmpty()) {
                    "This guild doesn't have any self-assignable roles."
                } else {
                    buildString {
                        if (!context.guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) {
                            append("**WARNING:** Bot lacks the ${Permission.MANAGE_ROLES.getName()} permission.")
                            return
                        }

                        context.data.roles.selfRoles.mapNotNull(context.guild::getRoleById)
                                .map(IMentionable::getAsMention)
                                .forEach { append("• ").append(it).append('\n') }
                    }
                }
            }
        }.action().queue()
    }
}