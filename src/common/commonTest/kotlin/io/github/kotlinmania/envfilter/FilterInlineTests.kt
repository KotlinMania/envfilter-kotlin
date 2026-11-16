// port-lint: tests filter.rs
package io.github.kotlinmania.envfilter.filter

import io.github.kotlinmania.envfilter.directive.Directive
import io.github.kotlinmania.envfilter.directive.enabled
import io.github.kotlinmania.log.Level
import io.github.kotlinmania.log.LevelFilter
import io.github.kotlinmania.snapbox.assertDataEq

public fun makeLoggerFilter(dirs: List<Directive>): Filter {
    val logger = Builder.new().build()
    logger.directives = dirs
    return logger
}

@kotlin.test.Test
public fun filterInfo() {
    val logger = Builder.new().filter(null, LevelFilter.Info).build()
    check(enabled(logger.directives, Level.Info, "crate1"))
    check(!enabled(logger.directives, Level.Debug, "crate1"))
}

@kotlin.test.Test
public fun filterBeginningLongestMatch() {
    val logger = Builder.new().filter("crate2", LevelFilter.Info).filter("crate2::mod", LevelFilter.Debug).filter("crate1::mod1", LevelFilter.Warn).build()
    check(enabled(logger.directives, Level.Debug, "crate2::mod1"))
    check(!enabled(logger.directives, Level.Debug, "crate2"))
}

// Some of our tests are only correct or complete when they cover the full
// universe of variants for log.Level. In the unlikely event that a new
// variant is added in the future, this test will detect the scenario and
// alert us to the need to review and update the tests. In such a
// situation, this test will fail to compile, and the error message will
// look something like this:
//
// error[E0004]: non-exhaustive patterns: `NewVariant` not covered
// --> src/filter/mod.rs:413:15
// |
// 413 |         match levelUniverse {
// |               ^^^^^^^^^^^^^^ pattern `NewVariant` not covered
@kotlin.test.Test
public fun ensureTestsCoverLevelUniverse() {
    val levelUniverse: Level = Level.Trace
    // use of trace variant is arbitrary
    when (levelUniverse) {
        Level.Error, Level.Warn, Level.Info, Level.Debug, Level.Trace -> Unit
    }
}

@kotlin.test.Test
public fun parseDefault() {
    val logger = Builder.new().parse("info,crate1::mod1=warn").build()
    check(enabled(logger.directives, Level.Warn, "crate1::mod1"))
    check(enabled(logger.directives, Level.Info, "crate2::mod2"))
}

@kotlin.test.Test
public fun parseDefaultBareLevelOffLc() {
    val logger = Builder.new().parse("off").build()
    check(!enabled(logger.directives, Level.Error, ""))
    check(!enabled(logger.directives, Level.Warn, ""))
    check(!enabled(logger.directives, Level.Info, ""))
    check(!enabled(logger.directives, Level.Debug, ""))
    check(!enabled(logger.directives, Level.Trace, ""))
}

@kotlin.test.Test
public fun parseDefaultBareLevelOffUc() {
    val logger = Builder.new().parse("OFF").build()
    check(!enabled(logger.directives, Level.Error, ""))
    check(!enabled(logger.directives, Level.Warn, ""))
    check(!enabled(logger.directives, Level.Info, ""))
    check(!enabled(logger.directives, Level.Debug, ""))
    check(!enabled(logger.directives, Level.Trace, ""))
}

@kotlin.test.Test
public fun parseDefaultBareLevelErrorLc() {
    val logger = Builder.new().parse("error").build()
    check(enabled(logger.directives, Level.Error, ""))
    check(!enabled(logger.directives, Level.Warn, ""))
    check(!enabled(logger.directives, Level.Info, ""))
    check(!enabled(logger.directives, Level.Debug, ""))
    check(!enabled(logger.directives, Level.Trace, ""))
}

@kotlin.test.Test
public fun parseDefaultBareLevelErrorUc() {
    val logger = Builder.new().parse("ERROR").build()
    check(enabled(logger.directives, Level.Error, ""))
    check(!enabled(logger.directives, Level.Warn, ""))
    check(!enabled(logger.directives, Level.Info, ""))
    check(!enabled(logger.directives, Level.Debug, ""))
    check(!enabled(logger.directives, Level.Trace, ""))
}

@kotlin.test.Test
public fun parseDefaultBareLevelWarnLc() {
    val logger = Builder.new().parse("warn").build()
    check(enabled(logger.directives, Level.Error, ""))
    check(enabled(logger.directives, Level.Warn, ""))
    check(!enabled(logger.directives, Level.Info, ""))
    check(!enabled(logger.directives, Level.Debug, ""))
    check(!enabled(logger.directives, Level.Trace, ""))
}

@kotlin.test.Test
public fun parseDefaultBareLevelWarnUc() {
    val logger = Builder.new().parse("WARN").build()
    check(enabled(logger.directives, Level.Error, ""))
    check(enabled(logger.directives, Level.Warn, ""))
    check(!enabled(logger.directives, Level.Info, ""))
    check(!enabled(logger.directives, Level.Debug, ""))
    check(!enabled(logger.directives, Level.Trace, ""))
}

@kotlin.test.Test
public fun parseDefaultBareLevelInfoLc() {
    val logger = Builder.new().parse("info").build()
    check(enabled(logger.directives, Level.Error, ""))
    check(enabled(logger.directives, Level.Warn, ""))
    check(enabled(logger.directives, Level.Info, ""))
    check(!enabled(logger.directives, Level.Debug, ""))
    check(!enabled(logger.directives, Level.Trace, ""))
}

@kotlin.test.Test
public fun parseDefaultBareLevelInfoUc() {
    val logger = Builder.new().parse("INFO").build()
    check(enabled(logger.directives, Level.Error, ""))
    check(enabled(logger.directives, Level.Warn, ""))
    check(enabled(logger.directives, Level.Info, ""))
    check(!enabled(logger.directives, Level.Debug, ""))
    check(!enabled(logger.directives, Level.Trace, ""))
}

@kotlin.test.Test
public fun parseDefaultBareLevelDebugLc() {
    val logger = Builder.new().parse("debug").build()
    check(enabled(logger.directives, Level.Error, ""))
    check(enabled(logger.directives, Level.Warn, ""))
    check(enabled(logger.directives, Level.Info, ""))
    check(enabled(logger.directives, Level.Debug, ""))
    check(!enabled(logger.directives, Level.Trace, ""))
}

@kotlin.test.Test
public fun parseDefaultBareLevelDebugUc() {
    val logger = Builder.new().parse("DEBUG").build()
    check(enabled(logger.directives, Level.Error, ""))
    check(enabled(logger.directives, Level.Warn, ""))
    check(enabled(logger.directives, Level.Info, ""))
    check(enabled(logger.directives, Level.Debug, ""))
    check(!enabled(logger.directives, Level.Trace, ""))
}

@kotlin.test.Test
public fun parseDefaultBareLevelTraceLc() {
    val logger = Builder.new().parse("trace").build()
    check(enabled(logger.directives, Level.Error, ""))
    check(enabled(logger.directives, Level.Warn, ""))
    check(enabled(logger.directives, Level.Info, ""))
    check(enabled(logger.directives, Level.Debug, ""))
    check(enabled(logger.directives, Level.Trace, ""))
}

@kotlin.test.Test
public fun parseDefaultBareLevelTraceUc() {
    val logger = Builder.new().parse("TRACE").build()
    check(enabled(logger.directives, Level.Error, ""))
    check(enabled(logger.directives, Level.Warn, ""))
    check(enabled(logger.directives, Level.Info, ""))
    check(enabled(logger.directives, Level.Debug, ""))
    check(enabled(logger.directives, Level.Trace, ""))
}

// In practice, the desired log level is typically specified by a token
// that is either all lowercase (e.g., 'trace') or all uppercase (.e.g,
// 'TRACE'), but this tests serves as a reminder that
// log.Level.fromStr() ignores all case variants.
@kotlin.test.Test
public fun parseDefaultBareLevelDebugMixed() {
    run {
        val logger = Builder.new().parse("Debug").build()
        check(enabled(logger.directives, Level.Error, ""))
        check(enabled(logger.directives, Level.Warn, ""))
        check(enabled(logger.directives, Level.Info, ""))
        check(enabled(logger.directives, Level.Debug, ""))
        check(!enabled(logger.directives, Level.Trace, ""))
    }
    run {
        val logger1 = Builder.new().parse("debuG").build()
        check(enabled(logger1.directives, Level.Error, ""))
        check(enabled(logger1.directives, Level.Warn, ""))
        check(enabled(logger1.directives, Level.Info, ""))
        check(enabled(logger1.directives, Level.Debug, ""))
        check(!enabled(logger1.directives, Level.Trace, ""))
    }
    run {
        val logger2 = Builder.new().parse("deBug").build()
        check(enabled(logger2.directives, Level.Error, ""))
        check(enabled(logger2.directives, Level.Warn, ""))
        check(enabled(logger2.directives, Level.Info, ""))
        check(enabled(logger2.directives, Level.Debug, ""))
        check(!enabled(logger2.directives, Level.Trace, ""))
    }
    run {
        val logger3 = Builder.new().parse("DeBuG").build()
        // LaTeX flavor!
        check(enabled(logger3.directives, Level.Error, ""))
        check(enabled(logger3.directives, Level.Warn, ""))
        check(enabled(logger3.directives, Level.Info, ""))
        check(enabled(logger3.directives, Level.Debug, ""))
        check(!enabled(logger3.directives, Level.Trace, ""))
    }
}

@kotlin.test.Test
public fun tryParseValidFilter() {
    val logger = Builder.new().tryParse("info,crate1::mod1=warn").expect("valid filter returned error").build()
    check(enabled(logger.directives, Level.Warn, "crate1::mod1"))
    check(enabled(logger.directives, Level.Info, "crate2::mod2"))
}

@kotlin.test.Test
public fun tryParseInvalidFilter() {
    val error = Builder.new().tryParse("info,crate1=invalid").unwrapErr()
    assertDataEq(error, str("error parsing logger filter: invalid logging spec 'invalid'"))
}

@kotlin.test.Test
public fun matchFullPath() {
    val logger = makeLoggerFilter(mutableListOf(Directive(name = "crate2".toOwned(), level = LevelFilter.Info), Directive(name = "crate1::mod1".toOwned(), level = LevelFilter.Warn)))
    check(enabled(logger.directives, Level.Warn, "crate1::mod1"))
    check(!enabled(logger.directives, Level.Info, "crate1::mod1"))
    check(enabled(logger.directives, Level.Info, "crate2"))
    check(!enabled(logger.directives, Level.Debug, "crate2"))
}

@kotlin.test.Test
public fun noMatch() {
    val logger = makeLoggerFilter(mutableListOf(Directive(name = "crate2".toOwned(), level = LevelFilter.Info), Directive(name = "crate1::mod1".toOwned(), level = LevelFilter.Warn)))
    check(!enabled(logger.directives, Level.Warn, "crate3"))
}

@kotlin.test.Test
public fun matchBeginning() {
    val logger = makeLoggerFilter(mutableListOf(Directive(name = "crate2".toOwned(), level = LevelFilter.Info), Directive(name = "crate1::mod1".toOwned(), level = LevelFilter.Warn)))
    check(enabled(logger.directives, Level.Info, "crate2::mod1"))
}

@kotlin.test.Test
public fun matchBeginningLongestMatch() {
    val logger = makeLoggerFilter(mutableListOf(Directive(name = "crate2".toOwned(), level = LevelFilter.Info), Directive(name = "crate2::mod".toOwned(), level = LevelFilter.Debug), Directive(name = "crate1::mod1".toOwned(), level = LevelFilter.Warn)))
    check(enabled(logger.directives, Level.Debug, "crate2::mod1"))
    check(!enabled(logger.directives, Level.Debug, "crate2"))
}

@kotlin.test.Test
public fun matchDefault() {
    val logger = makeLoggerFilter(mutableListOf(Directive(name = null, level = LevelFilter.Info), Directive(name = "crate1::mod1".toOwned(), level = LevelFilter.Warn)))
    check(enabled(logger.directives, Level.Warn, "crate1::mod1"))
    check(enabled(logger.directives, Level.Info, "crate2::mod2"))
}

@kotlin.test.Test
public fun zeroLevel() {
    val logger = makeLoggerFilter(mutableListOf(Directive(name = null, level = LevelFilter.Info), Directive(name = "crate1::mod1".toOwned(), level = LevelFilter.Off)))
    check(!enabled(logger.directives, Level.Error, "crate1::mod1"))
    check(enabled(logger.directives, Level.Info, "crate2::mod2"))
}
