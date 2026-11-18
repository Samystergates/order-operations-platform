export function normalizeCombination(combination) {
  return (combination || "")
    .split(",")
    .map((part) => {
      const word = part.trim().toLowerCase();
      if (word.startsWith("pri") || word.startsWith("pre")) {
        return "primer";
      }
      return word;
    })
    .filter(Boolean)
    .sort()
    .join(",");
}

export function findSpuPriceCode(priceCodes, selectedRadio, checkedCombinations, zoeknaam) {
  const zoeknaamNumber = parseInt(zoeknaam?.substring(0, 2), 10);
  const spuType = selectedRadio === "poedercoaten" ? "POEDERCOATEN" : "NATLAKKEN";

  const filtered = (priceCodes || []).filter(
    (entry) =>
      entry.spuOrderType === spuType &&
      zoeknaamNumber >= entry.startNumber &&
      zoeknaamNumber <= entry.endNumber
  );

  if (spuType === "POEDERCOATEN") {
    return filtered[0]?.code || null;
  }

  const checked = normalizeCombination((checkedCombinations || []).join(","));

  for (const entry of filtered) {
    if (!entry.combinations) continue;

    const acceptedCombinations = entry.combinations
      .split("|")
      .map(normalizeCombination);

    if (acceptedCombinations.includes(checked)) {
      return entry.code;
    }
  }

  return null;
}

export function shouldUseWhiteWheelImage({ red = 0, green = 0, blue = 0 } = {}) {
  return (
    (red === 0 && green === 0 && blue === 0) ||
    (red === 42 && green === 41 && blue === 42) ||
    (red === 39 && green === 41 && blue === 43) ||
    (red === 14 && green === 14 && blue === 16) ||
    (red === 43 && green === 43 && blue === 44) ||
    (red === 26 && green === 23 && blue === 25) ||
    (red === 48 && green === 50 && blue === 52) ||
    (red <= 40 && green <= 40 && blue <= 40) ||
    (red <= 50 && green <= 40 && blue <= 40) ||
    (red <= 40 && green <= 50 && blue <= 40) ||
    (red <= 40 && green <= 40 && blue <= 50)
  );
}
