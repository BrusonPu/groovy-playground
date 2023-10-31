import groovy.io.FileType

// replace full-width spaces in a file
def replaceFullWidthSpacesInFile(file) {
    def text = file.text
    def updatedText = text.replaceAll(/\p{Zs}/, " ") // Replaces full-width spaces with regular spaces
    if (text != updatedText) {
        file.text = updatedText
        println("Replaced full-width spaces in: ${file.absolutePath}")
    }
}

// recursively process .csproj .vbproj files in a directory
def processCsprojFilesInDirectory(directories) {
    println("Folder Detected: ${directories}")
    for (File directory in directories) { 
        def csprojFiles = directory.listFiles({ file -> file.name.endsWith('.csproj') || file.name.endsWith('.vbproj') })
        csprojFiles.each { file ->
            replaceFullWidthSpacesInFile(file)
        }
    }
}
// get all subfolders except .git directory
def getSubDir(){
    def currentFolder = new File(".")
    def dirs = [] << currentFolder
    currentFolder.eachFileRecurse (FileType.DIRECTORIES) { dir -> 
        if (!( dir ==~ /\.\/\.git.*/ ))
            { dirs << dir }
    }
    return dirs
}

// Start processing .csproj .vbproj files from the current folder and its subfolders
processCsprojFilesInDirectory(getSubDir())