import React from "react";

const childColumns = [
  "Regel",
  "Backorder",
  "SME",
  "SPU",
  "Aantal",
  "Artikelnummer",
  "Omschrijving",
  "Leverdatum",
  "Gebruiker",
  "MON LB",
  "MON TR",
  "MWE",
  "SER",
  "TRA",
  "EXP",
];

export default function OrderChildTableHeader() {
  return (
    <thead>
      <tr>
        {childColumns.map((column) => (
          <th key={column}>{column}</th>
        ))}
      </tr>
    </thead>
  );
}
