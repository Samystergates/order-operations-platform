import { getFlowFieldName, getNextFlowStatus } from "./orderFlowTransitions";

describe("getFlowFieldName", () => {
  it("uses the API property casing for five-character department names", () => {
    expect(getFlowFieldName("MONTR")).toBe("monTr");
    expect(getFlowFieldName("SME")).toBe("sme");
  });
});

describe("getNextFlowStatus", () => {
  it("advances and reverses the SME/SPU start state", () => {
    expect(getNextFlowStatus("", "FWD", null, true)).toBe("R");
    expect(getNextFlowStatus("R", "RVS", null, true)).toBe("");
  });

  it("advances, halts, and restores a previous status", () => {
    expect(getNextFlowStatus("R", "FWD", null)).toBe("Y");
    expect(getNextFlowStatus("Y", "FWD", null)).toBe("G");
    expect(getNextFlowStatus("Y", "HLT", null)).toBe("B");
    expect(getNextFlowStatus("B", "RVS", "Y")).toBe("Y");
  });

  it("does not apply the SME/SPU initial transition to transport flows", () => {
    expect(getNextFlowStatus("", "FWD", null)).toBe("");
    expect(getNextFlowStatus("R", "RVS", null)).toBe("R");
  });
});
