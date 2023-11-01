import com.atlassian.bitbucket.hook.HookResponse
import com.atlassian.bitbucket.repository.RefChange
import com.atlassian.bitbucket.hook.ScriptHook
import com.atlassian.bitbucket.io.LineReader
import com.atlassian.bitbucket.io.LineReader.OutputHandler
import com.atlassian.bitbucket.io.LineWriter
import com.atlassian.bitbucket.util.ResultConsumer
import com.atlassian.bitbucket.scm.Command
import com.atlassian.bitbucket.scm.CommandBuilderSupport
import com.atlassian.bitbucket.scm.git.command.GitCommand
import com.atlassian.bitbucket.scm.git.command.GitScmCommandBuilder

class FullWidthSpaceReplaceHook extends ScriptHook {
    @Override
    boolean shouldRefBeUpdated(RefChange refChange, HookResponse hookResponse) {
        // Define the regex pattern to match full-width spaces
        def fullWidthSpacePattern = /\p{Zs}/

        // Define the repository
        def repository = refChange.repository

        // Define the command builder
        def commandBuilder = new GitScmCommandBuilder.Builder(repository)
            .command("ls-files")
            .argument("-z")
            .build()

        // Execute the 'ls-files' command to list all tracked files
        def trackedFiles = new ArrayList<String>()
        commandBuilder.getCommand().pipe(new OutputHandler() {
            @Override
            void process(Reader reader) {
                new LineReader(reader).eachLine { line ->
                    trackedFiles.add(line.replaceAll("\u0000", ""))
                }
            }
        })

        // Loop through the tracked files and check if they are .csproj files
        trackedFiles.each { filePath ->
            if (filePath.endsWith(".csproj")) {
                def file = new File(repository.getDirectory(), filePath)
                if (file.exists()) {
                    // Read the content of the file
                    def fileContent = file.text

                    // Replace full-width spaces with regular spaces
                    def updatedContent = fileContent.replaceAll(fullWidthSpacePattern, " ")

                    if (fileContent != updatedContent) {
                        // Update the file with the modified content
                        file.text = updatedContent
                        hookResponse.out().write("Replaced full-width spaces in: $filePath\n")
                    }
                }
            }
        }

        return true
    }
}

// Create an instance of the hook and register it with Bitbucket
def hook = new FullWidthSpaceReplaceHook()
hook.register()