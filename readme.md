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
