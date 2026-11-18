const SORT_FIELDS = {
  Naam: "customerName",
  Postcode: "postCode",
  Backorder: "backOrder",
  Verkooporder: "orderNumber",
  Plaats: "city",
  Land: "country",
  Leverdatum: "deliveryDate",
  Gebruiker: "verifierUser",
  datumorder: "creationDate",
};

export const ORDER_SORT_STATE_KEYS = {
  Naam: "name",
  Postcode: "postcode",
  Backorder: "backorder",
  Verkooporder: "orderNumber",
  Plaats: "city",
  Land: "country",
  Leverdatum: "deliveryDate",
  Gebruiker: "user",
  datumorder: "creationDate",
};

const nextDirection = (direction) => (direction === "Asc" ? "Dsc" : "Asc");

const compareText = (field, direction) => (left, right) => {
  const result = String(left[field] ?? "").localeCompare(String(right[field] ?? ""));
  return direction === "Asc" ? result : -result;
};

const compareDate = (field, direction) => (left, right) => {
  const leftTime = left[field] ? new Date(left[field]).getTime() : NaN;
  const rightTime = right[field] ? new Date(right[field]).getTime() : NaN;
  const leftHasDate = Number.isFinite(leftTime);
  const rightHasDate = Number.isFinite(rightTime);

  if (!leftHasDate && !rightHasDate) return 0;
  if (!leftHasDate) return direction === "Asc" ? 1 : -1;
  if (!rightHasDate) return direction === "Asc" ? -1 : 1;
  return direction === "Asc" ? leftTime - rightTime : rightTime - leftTime;
};

const sortBackorderGroups = (rows, direction) => {
  const groups = new Map();
  rows.forEach((row) => {
    const group = groups.get(row.orderNumber) || [];
    group.push(row);
    groups.set(row.orderNumber, group);
  });

  return [...groups.values()]
    .map((group) => group.sort(compareText("backOrder", "Asc")))
    .sort((left, right) =>
      compareText("backOrder", direction)(left[0], right[0])
    )
    .flat();
};

export function sortOrderRows(rows, column, currentDirection = "Asc", requestedDirection) {
  if (column === "EXC") {
    return {
      rows: rows.filter((row) => row.exclamation === "JA"),
      nextDirection: currentDirection,
    };
  }

  const field = SORT_FIELDS[column];
  if (!field) {
    return { rows, nextDirection: currentDirection };
  }

  const direction = requestedDirection
    ? nextDirection(requestedDirection)
    : currentDirection;
  const sortedRows =
    column === "Backorder"
      ? sortBackorderGroups(rows, direction)
      : [...rows].sort(
          field === "deliveryDate" || field === "creationDate"
            ? compareDate(field, direction)
            : compareText(field, direction)
        );

  return {
    rows: sortedRows,
    nextDirection: requestedDirection || nextDirection(currentDirection),
  };
}
