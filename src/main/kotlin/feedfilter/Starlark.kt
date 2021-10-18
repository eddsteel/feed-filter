package com.eddsteel.feedfilter

import com.google.common.collect.ImmutableMap
import net.starlark.java.eval.Module
import net.starlark.java.eval.Mutability
import net.starlark.java.eval.Starlark
import net.starlark.java.eval.StarlarkSemantics
import net.starlark.java.eval.StarlarkThread
import net.starlark.java.syntax.Expression
import net.starlark.java.syntax.FileOptions
import net.starlark.java.syntax.ParserInput
import net.starlark.java.syntax.Program
import net.starlark.java.syntax.StarlarkFile

// TODO: build program once. Add episode to environment not program
class StarlarkPredicate(val rule: String) {
    fun test(episode: EpisodeMetadata): Boolean {
        Mutability.create("scope").use { mu ->
            val semantics = StarlarkSemantics.DEFAULT
            val thread = StarlarkThread(mu, semantics)
            val input = ParserInput.fromLines(
                "episode = ${episode.stl}",
                "def rule(episode): $rule",
                "rule(episode)")

            val output = Starlark.execFile(input, FileOptions.DEFAULT, emptyMap(), thread)
            return output as Boolean
        }
    }
}
