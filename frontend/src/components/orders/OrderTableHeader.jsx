import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFilter } from "@fortawesome/free-solid-svg-icons";
import FilterDepDropdown from "../FilterDepDropdown";
import OrderUserFilterDropdown from "./OrderUserFilterDropdown";

const headerFilterStyle = {
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
};

const filterIconStyle = (active) => ({
  marginTop: "5px",
  color: active ? "black" : "#7E8D85",
});

const departmentFilters = [
  ["SME", "SME", "sme"],
  ["SPU", "SPU", "spu"],
  ["MON LB", "MONLB", "ml"],
  ["MON TR", "MONTR", "mt"],
  ["MWE", "MWE", "mwe"],
  ["SER", "SER", "ser"],
  ["TRA", "TRA", "tra"],
  ["EXP", "EXP", "exp"],
];

function SortableHeader({ label, filterName, state, onFilterClick }) {
  return (
    <th onClick={() => onFilterClick(filterName)}>
      <div style={headerFilterStyle}>
        <span>{label}</span>
        <FontAwesomeIcon icon={faFilter} style={filterIconStyle(state === "Asc")} />
      </div>
    </th>
  );
}

export default function OrderTableHeader({
  filterState,
  filterSortStates,
  departmentDropdowns,
  userFilter,
  colorStateExc,
  handleColorChange,
  applyFilters,
  resetFilterForColumn,
  isFilterActive,
  handleFilterClick,
}) {
  return (
    <thead>
      <tr>
        <th></th>
        <SortableHeader
          label="Verkooporder"
          filterName="Verkooporder"
          state={filterSortStates.orderNumber}
          onFilterClick={handleFilterClick}
        />
        <th>Ordersoort</th>
        <SortableHeader
          label="Backorder"
          filterName="Backorder"
          state={filterSortStates.backorder}
          onFilterClick={handleFilterClick}
        />

        {departmentFilters.map(([label, filterKey, dropdownKey]) => (
          <FilterDepDropdown
            key={filterKey}
            label={label}
            filterKey={filterKey}
            isOpen={departmentDropdowns[dropdownKey].isOpen}
            toggle={departmentDropdowns[dropdownKey].toggle}
            filterState={filterState}
            handleColorChange={handleColorChange}
            applyFilters={applyFilters}
            resetFilterForColumn={resetFilterForColumn}
            isFilterActive={isFilterActive}
          />
        ))}

        <th onClick={() => handleFilterClick("EXC")}>
          <div style={headerFilterStyle}>
            <span>!</span>
            <FontAwesomeIcon icon={faFilter} style={filterIconStyle(colorStateExc === "D")} />
          </div>
        </th>

        <OrderUserFilterDropdown {...userFilter} />
        <th>Organisatie</th>
        <SortableHeader
          label="Naam"
          filterName="Naam"
          state={filterSortStates.name}
          onFilterClick={handleFilterClick}
        />
        <SortableHeader
          label="Postcode"
          filterName="Postcode"
          state={filterSortStates.postcode}
          onFilterClick={handleFilterClick}
        />
        <SortableHeader
          label="Plaats"
          filterName="Plaats"
          state={filterSortStates.city}
          onFilterClick={handleFilterClick}
        />
        <SortableHeader
          label="Land"
          filterName="Land"
          state={filterSortStates.country}
          onFilterClick={handleFilterClick}
        />
        <SortableHeader
          label="Leverdatum"
          filterName="Leverdatum"
          state={filterSortStates.deliveryDate}
          onFilterClick={handleFilterClick}
        />
        <th>Referentie</th>
        <SortableHeader
          label="Datum order"
          filterName="datumorder"
          state={filterSortStates.creationDate}
          onFilterClick={handleFilterClick}
        />
        <SortableHeader
          label="Gebruiker (L)"
          filterName="Gebruiker"
          state={filterSortStates.user}
          onFilterClick={handleFilterClick}
        />
      </tr>
    </thead>
  );
}
