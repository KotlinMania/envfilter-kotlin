#if canImport(Testing)
import Testing
import Envfilter

@Suite("Envfilter Swift Export Tests")
struct EnvfilterExportTests {
    @Test("Envfilter swift module imported cleanly")
    func testSwiftModuleLoads() throws {
        #expect(Bool(true), "Envfilter swift module imported cleanly")
    }
}
#elseif canImport(XCTest)
import XCTest
import Envfilter

final class EnvfilterExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "Envfilter swift module imported cleanly")
    }
}
#endif
