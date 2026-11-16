// port-lint: tests parser.rs
package io.github.kotlinmania.envfilter.parser

import io.github.kotlinmania.log.LevelFilter
import io.github.kotlinmania.snapbox.Data
import io.github.kotlinmania.snapbox.assertDataEq

public fun ParseError.intoData(): Data = this.toString().intoData()

@kotlin.test.Test
public fun parseSpecValid() {
    val structVal = parseSpec("crate1::mod1=error,crate1::mod2,crate2=debug")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    check(dirs.size == 3)
    check(dirs[0].name == "crate1::mod1".toOwned())
    check(dirs[0].level == LevelFilter.Error)
    check(dirs[1].name == "crate1::mod2".toOwned())
    check(dirs[1].level == LevelFilter.max())
    check(dirs[2].name == "crate2".toOwned())
    check(dirs[2].level == LevelFilter.Debug)
    check(filter == null)
    check(errors.isEmpty())
}

@kotlin.test.Test
public fun parseSpecInvalidCrate() {
    // test parseSpec with multiple = in specification
    val structVal = parseSpec("crate1::mod1=warn=info,crate2=debug")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    check(dirs.size == 1)
    check(dirs[0].name == "crate2".toOwned())
    check(dirs[0].level == LevelFilter.Debug)
    check(filter == null)
    check(errors.size == 1)
    assertDataEq(errors[0], str("invalid logging spec 'crate1::mod1=warn=info'"))
}

@kotlin.test.Test
public fun parseSpecInvalidLevel() {
    // test parseSpec with 'noNumber' as log level
    val structVal = parseSpec("crate1::mod1=noNumber,crate2=debug")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    check(dirs.size == 1)
    check(dirs[0].name == "crate2".toOwned())
    check(dirs[0].level == LevelFilter.Debug)
    check(filter == null)
    check(errors.size == 1)
    assertDataEq(errors[0], str("invalid logging spec 'noNumber'"))
}

@kotlin.test.Test
public fun parseSpecStringLevel() {
    // test parseSpec with 'warn' as log level
    val structVal = parseSpec("crate1::mod1=wrong,crate2=warn")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    check(dirs.size == 1)
    check(dirs[0].name == "crate2".toOwned())
    check(dirs[0].level == LevelFilter.Warn)
    check(filter == null)
    check(errors.size == 1)
    assertDataEq(errors[0], str("invalid logging spec 'wrong'"))
}

@kotlin.test.Test
public fun parseSpecEmptyLevel() {
    // test parseSpec with '' as log level
    val structVal = parseSpec("crate1::mod1=wrong,crate2=")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    check(dirs.size == 1)
    check(dirs[0].name == "crate2".toOwned())
    check(dirs[0].level == LevelFilter.max())
    check(filter == null)
    check(errors.size == 1)
    assertDataEq(errors[0], str("invalid logging spec 'wrong'"))
}

@kotlin.test.Test
public fun parseSpecEmptyLevelIsolated() {
    // test parseSpec with "" as log level (and the entire spec str)
    val structVal = parseSpec("")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    // should be ignored
    check(dirs.size == 0)
    check(filter == null)
    check(errors.isEmpty())
}

@kotlin.test.Test
public fun parseSpecBlankLevelIsolated() {
    // test parseSpec with a white-space-only string specified as the log
    // level (and the entire spec str)
    val structVal = parseSpec("     ")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    // should be ignored
    check(dirs.size == 0)
    check(filter == null)
    check(errors.isEmpty())
}

@kotlin.test.Test
public fun parseSpecBlankLevelIsolatedCommaOnly() {
    // The spec should contain zero or more comma-separated string slices,
    // so a comma-only string should be interpreted as two empty strings
    // (which should both be treated as invalid, so ignored).
    val structVal = parseSpec(",")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    // should be ignored
    check(dirs.size == 0)
    check(filter == null)
    check(errors.isEmpty())
}

@kotlin.test.Test
public fun parseSpecBlankLevelIsolatedCommaBlank() {
    // The spec should contain zero or more comma-separated string slices,
    // so this bogus spec should be interpreted as containing one empty
    // string and one blank string. Both should both be treated as
    // invalid, so ignored.
    val structVal = parseSpec(",     ")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    // should be ignored
    check(dirs.size == 0)
    check(filter == null)
    check(errors.isEmpty())
}

@kotlin.test.Test
public fun parseSpecBlankLevelIsolatedBlankComma() {
    // The spec should contain zero or more comma-separated string slices,
    // so this bogus spec should be interpreted as containing one blank
    // string and one empty string. Both should both be treated as
    // invalid, so ignored.
    val structVal = parseSpec("     ,")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    // should be ignored
    check(dirs.size == 0)
    check(filter == null)
    check(errors.isEmpty())
}

@kotlin.test.Test
public fun parseSpecGlobal() {
    // test parseSpec with no crate
    val structVal = parseSpec("warn,crate2=debug")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    check(dirs.size == 2)
    check(dirs[0].name == null)
    check(dirs[0].level == LevelFilter.Warn)
    check(dirs[1].name == "crate2".toOwned())
    check(dirs[1].level == LevelFilter.Debug)
    check(filter == null)
    check(errors.isEmpty())
}

@kotlin.test.Test
public fun parseSpecGlobalBareWarnLc() {
    // test parseSpec with no crate, in isolation, all lowercase
    val structVal = parseSpec("warn")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    check(dirs.size == 1)
    check(dirs[0].name == null)
    check(dirs[0].level == LevelFilter.Warn)
    check(filter == null)
    check(errors.isEmpty())
}

@kotlin.test.Test
public fun parseSpecGlobalBareWarnUc() {
    // test parseSpec with no crate, in isolation, all uppercase
    val structVal = parseSpec("WARN")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    check(dirs.size == 1)
    check(dirs[0].name == null)
    check(dirs[0].level == LevelFilter.Warn)
    check(filter == null)
    check(errors.isEmpty())
}

@kotlin.test.Test
public fun parseSpecGlobalBareWarnMixed() {
    // test parseSpec with no crate, in isolation, mixed case
    val structVal = parseSpec("wArN")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    check(dirs.size == 1)
    check(dirs[0].name == null)
    check(dirs[0].level == LevelFilter.Warn)
    check(filter == null)
    check(errors.isEmpty())
}

@kotlin.test.Test
public fun parseSpecValidFilter() {
    val structVal = parseSpec("crate1::mod1=error,crate1::mod2,crate2=debug/abc")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    check(dirs.size == 3)
    check(dirs[0].name == "crate1::mod1".toOwned())
    check(dirs[0].level == LevelFilter.Error)
    check(dirs[1].name == "crate1::mod2".toOwned())
    check(dirs[1].level == LevelFilter.max())
    check(dirs[2].name == "crate2".toOwned())
    check(dirs[2].level == LevelFilter.Debug)
    check(filter != null && filter!!.toString() == "abc")
    check(errors.isEmpty())
}

@kotlin.test.Test
public fun parseSpecInvalidCrateFilter() {
    val structVal = parseSpec("crate1::mod1=error=warn,crate2=debug/a.c")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    check(dirs.size == 1)
    check(dirs[0].name == "crate2".toOwned())
    check(dirs[0].level == LevelFilter.Debug)
    check(filter != null && filter!!.toString() == "a.c")
    check(errors.size == 1)
    assertDataEq(errors[0], str("invalid logging spec 'crate1::mod1=error=warn'"))
}

@kotlin.test.Test
public fun parseSpecEmptyWithFilter() {
    val structVal = parseSpec("crate1/a*c")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    check(dirs.size == 1)
    check(dirs[0].name == "crate1".toOwned())
    check(dirs[0].level == LevelFilter.max())
    check(filter != null && filter!!.toString() == "a*c")
    check(errors.isEmpty())
}

@kotlin.test.Test
public fun parseSpecWithMultipleFilters() {
    val structVal = parseSpec("debug/abc/a.c")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    check(dirs.isEmpty())
    check(filter == null)
    check(errors.size == 1)
    assertDataEq(errors[0], str("invalid logging spec 'debug/abc/a.c' (too many '/'s)"))
}

@kotlin.test.Test
public fun parseSpecMultipleInvalidCrates() {
    // test parseSpec with multiple = in specification
    val structVal = parseSpec("crate1::mod1=warn=info,crate2=debug,crate3=error=error")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    check(dirs.size == 1)
    check(dirs[0].name == "crate2".toOwned())
    check(dirs[0].level == LevelFilter.Debug)
    check(filter == null)
    check(errors.size == 2)
    assertDataEq(errors[0], str("invalid logging spec 'crate1::mod1=warn=info'"))
    assertDataEq(errors[1], str("invalid logging spec 'crate3=error=error'"))
}

@kotlin.test.Test
public fun parseSpecMultipleInvalidLevels() {
    // test parseSpec with 'noNumber' as log level
    val structVal = parseSpec("crate1::mod1=noNumber,crate2=debug,crate3=invalid")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    check(dirs.size == 1)
    check(dirs[0].name == "crate2".toOwned())
    check(dirs[0].level == LevelFilter.Debug)
    check(filter == null)
    check(errors.size == 2)
    assertDataEq(errors[0], str("invalid logging spec 'noNumber'"))
    assertDataEq(errors[1], str("invalid logging spec 'invalid'"))
}

@kotlin.test.Test
public fun parseSpecInvalidCrateAndLevel() {
    // test parseSpec with 'noNumber' as log level
    val structVal = parseSpec("crate1::mod1=debug=info,crate2=debug,crate3=invalid")
    val dirs = structVal.directives
    val filter = structVal.filter
    val errors = structVal.errors
    check(dirs.size == 1)
    check(dirs[0].name == "crate2".toOwned())
    check(dirs[0].level == LevelFilter.Debug)
    check(filter == null)
    check(errors.size == 2)
    assertDataEq(errors[0], str("invalid logging spec 'crate1::mod1=debug=info'"))
    assertDataEq(errors[1], str("invalid logging spec 'invalid'"))
}

@kotlin.test.Test
public fun parseErrorMessageSingleError() {
    val error = parseSpec("crate1::mod1=debug=info,crate2=debug").ok().unwrapErr()
    assertDataEq(error, str("error parsing logger filter: invalid logging spec 'crate1::mod1=debug=info'"))
}

@kotlin.test.Test
public fun parseErrorMessageMultipleErrors() {
    val error = parseSpec("crate1::mod1=debug=info,crate2=debug,crate3=invalid").ok().unwrapErr()
    assertDataEq(error, str("error parsing logger filter: invalid logging spec 'crate1::mod1=debug=info'"))
}
