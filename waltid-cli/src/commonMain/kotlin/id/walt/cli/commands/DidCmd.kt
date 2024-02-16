package id.walt.cli.commands

import com.github.ajalt.clikt.core.CliktCommand

class DidCmd : CliktCommand(
    name = "did",
    help = "DID management features",
    printHelpOnEmptyArgs = true
) {

    init {
        // subcommands(KeyGenerateCmd(), KeyConvertCmd())
    }

    override fun run(): Unit {}
}

fun main(args: Array<String>) = DidCmd().main(args)
