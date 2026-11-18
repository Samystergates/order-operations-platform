import {
  findSpuPriceCode,
  normalizeCombination,
  shouldUseWhiteWheelImage,
} from "./spuModalLogic";

describe("spuModalLogic", () => {
  it("normalizes primer typo variants in combinations", () => {
    expect(normalizeCombination("Aflakken, premer")).toBe("aflakken,primer");
  });

  it("uses the first matching poedercoaten price code", () => {
    expect(
      findSpuPriceCode(
        [{ spuOrderType: "POEDERCOATEN", startNumber: 10, endNumber: 20, code: "P1" }],
        "poedercoaten",
        [],
        "12ABC"
      )
    ).toBe("P1");
  });

  it("matches natlakken price codes by normalized required combinations", () => {
    expect(
      findSpuPriceCode(
        [
          {
            spuOrderType: "NATLAKKEN",
            startNumber: 10,
            endNumber: 20,
            code: "N1",
            combinations: "Primer,Aflakken|Schooperen",
          },
        ],
        "natLakken",
        ["aflakken", "premer"],
        "12ABC"
      )
    ).toBe("N1");
  });

  it("chooses the white wheel asset for dark colors", () => {
    expect(shouldUseWhiteWheelImage({ red: 20, green: 20, blue: 20 })).toBe(true);
    expect(shouldUseWhiteWheelImage({ red: 200, green: 20, blue: 20 })).toBe(false);
  });
});
