export const ORDER_FILTER_KEY_MAPPING = {
  SME: "sme",
  SPU: "spu",
  MONLB: "monLb",
  MONTR: "monTr",
  MWE: "mwe",
  SER: "ser",
  TRA: "tra",
  EXP: "exp",
};

const COLOR_PRIORITY = ["B", "R", "Y", "G", ""];

export const isOrderFilterActive = (filters, column, maxStates = 5) => {
  const selected = filters[column] || [];
  return selected.length > 0 && selected.length < maxStates;
};

export const applyOrderColorFilters = (
  data,
  filters,
  keyMapping = ORDER_FILTER_KEY_MAPPING
) => {
  const activeFilters = Object.entries(filters).filter(
    ([, values]) => Array.isArray(values) && values.length > 0
  );

  if (activeFilters.length === 0) {
    return data;
  }

  const grouped = data.reduce((acc, row) => {
    (acc[row.orderNumber] ||= []).push(row);
    return acc;
  }, {});

  const passingOrders = new Set(
    Object.entries(grouped)
      .filter(([, rows]) =>
        activeFilters.every(([column, allowed]) => {
          const key = keyMapping[column];
          const normalizedAllowed = allowed.map((value) =>
            value == null ? "" : value.toString().trim().toUpperCase()
          );

          const includeEmptyMatch = normalizedAllowed.includes("");
          const values = rows.map((row) => {
            const rawValue = row[key];
            if (rawValue == null) {
              return includeEmptyMatch ? "" : null;
            }
            return rawValue.toString().trim().toUpperCase();
          });

          if (values.every((value) => value === null)) {
            return false;
          }

          let highest = undefined;
          for (const color of COLOR_PRIORITY) {
            if (color === "") {
              if (
                values.includes("") ||
                (includeEmptyMatch && values.includes(null))
              ) {
                highest = "";
                break;
              }
            } else if (values.includes(color)) {
              highest = color;
              break;
            }
          }

          if (highest === undefined) {
            return false;
          }

          return normalizedAllowed.includes(highest);
        })
      )
      .map(([orderNumber]) => orderNumber)
  );

  return data.filter((row) => passingOrders.has(row.orderNumber));
};

export const applyOrderSearchFilter = (data, searchTerm) => {
  if (searchTerm == null || searchTerm.toString().trim() === "") {
    return data;
  }

  const normalizedSearchTerm = searchTerm.toString().toLowerCase();
  return (data || []).filter((item) => {
    const customerName = item?.customerName?.toString().toLowerCase() || "";
    const city = item?.city?.toString().toLowerCase() || "";
    const orderNumber = item?.orderNumber?.toString().toLowerCase() || "";

    return customerName.includes(normalizedSearchTerm)
      || city.includes(normalizedSearchTerm)
      || orderNumber.includes(normalizedSearchTerm);
  });
};

export const applyOrderUserFilter = (data, selectedUsers) => {
  if (!Array.isArray(selectedUsers)) {
    return data || [];
  }

  return (data || []).filter((item) =>
    selectedUsers.includes(item?.user)
  );
};

export const applyCombinedOrderFilters = (
  data,
  searchTerm,
  selectedUsers
) => applyOrderUserFilter(
  applyOrderSearchFilter(data, searchTerm),
  selectedUsers
);

export const paginateOrders = (data, currentPage, entriesPerPage) => {
  const safeEntriesPerPage = Math.max(1, Number(entriesPerPage) || 1);
  const safePage = Math.max(1, Number(currentPage) || 1);
  const startIndex = (safePage - 1) * safeEntriesPerPage;

  return (data || []).slice(startIndex, startIndex + safeEntriesPerPage);
};
