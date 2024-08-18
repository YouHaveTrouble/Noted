## Noted
Instance-scoped application that allows you to create canned responses and post them to any channel that allows that.

### Explanation
Instance-scoped means that each instance of the bot will have its own set of notes. No matter where the app is installed,
the same set of notes will be available. If you want to have different set of notes, you need to host a separate
instance of the bot.

### Usage

#### Users
All users that have the app installed can use the /note (command name configurable by admin) command to retrieve a note.

#### Admin
Only configured admin user can add, edit or delete notes. To add a note, you need to have your user id specified in the
noted.properties configuration file. You can specify multiple user ids separated by `,`. You can use add-note command to
add a note, edit-note to edit a note, delete-note to delete a note. Those commands are only available in the direct
message channel with the bot.

### Configuration

```properties
DISCORD_TOKEN=your_bot_token_here
ADMIN_USER_ID=your_user_id_here,another_user_id_here
COMMAND=note
```

#### DISCORD_TOKEN
This is the token that you get when you create a bot in the [discord developer portal](https://discord.com/developers).

#### ADMIN_USER_ID
This is the user id of the admin user. You can get your user id by enabling developer mode in discord and right-clicking
on your user in the user list. You can specify multiple user ids separated by `,`.

#### COMMAND
This is the command that users will use to retrieve a note. You can change this to any command you want except existing
admin commands. If you set this to any admin command, default `note` will be used.

### Building

To build the app, you need to have maven installed. Run the following command to build the app:

```shell
mvn clean package
```

This will create a jar file in the /target directory. You can run the jar using java 21 or higher:
