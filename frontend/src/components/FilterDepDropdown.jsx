import React from 'react';
import {
  Dropdown,
  DropdownToggle,
  DropdownMenu,
  DropdownItem
} from 'reactstrap';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faFilter } from '@fortawesome/free-solid-svg-icons';

const FilterDepDropdown = ({
  label,
  filterKey,
  isOpen,
  toggle,
  filterState,
  handleColorChange,
  applyFilters,
  resetFilterForColumn,
  isFilterActive
}) => {
  const colors = [
    { code: 'R', bg: 'rgb(204, 80, 80)' },
    { code: 'Y', bg: 'rgb(223, 212, 62)' },
    { code: 'G', bg: 'rgb(52, 151, 52)' },
    { code: 'B', bg: 'rgb(64, 64, 206)' },
  ];

  return (
    <th>
      <Dropdown isOpen={isOpen} toggle={toggle} container="body">
        <DropdownToggle data-toggle="dropdown" tag="span">
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <span>{label}</span>
            <FontAwesomeIcon
              icon={faFilter}
              style={{
                marginTop: '5px',
                color: isFilterActive(filterKey) ? '#007BFF' : 'black'
              }}
            />
          </div>
        </DropdownToggle>
        <DropdownMenu style={{
          maxHeight: "250px", overflowY: "auto", padding: "10px", width: "180px", zIndex: 9999,
          position: "fixed"
        }}>
          {/* Empty */}
          <DropdownItem toggle={false} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', width: '100%' }}>
              <input
                type="checkbox"
                checked={filterState[filterKey].includes("")}
                onChange={() => handleColorChange(filterKey, "")}
                style={{ transform: 'scale(1.6)', cursor: 'pointer' }}
              />
              <span style={{ marginLeft: '10px' }}>Lege Cellen</span>
            </label>
          </DropdownItem>

          {/* Colors */}
          {colors.map(({ code, bg }) => (
            <DropdownItem key={code} toggle={false} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '8px', width: '100%', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={filterState[filterKey].includes(code)}
                  onChange={() => handleColorChange(filterKey, code)}
                  style={{ transform: 'scale(1.6)', cursor: 'pointer' }}
                />
                <div style={{
                  width: '90%',
                  height: '20px',
                  backgroundColor: bg,
                  borderRadius: '4px',
                  border: '1px solid #ccc'
                }}></div>
              </label>
            </DropdownItem>

          ))}

          <DropdownItem divider />

          {/* Buttons */}
          <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0 5px' }}>
            <button
              onClick={() => {
                applyFilters();
                toggle();
              }}
              style={{
                backgroundColor: '#007BFF',
                border: 'none',
                color: 'white',
                padding: '6px 12px',
                borderRadius: '5px',
                cursor: 'pointer'
              }}
            >
              Apply
            </button>
            <button
              onClick={() => resetFilterForColumn(filterKey)}
              style={{
                backgroundColor: '#6c757d',
                border: 'none',
                color: 'white',
                padding: '6px 12px',
                borderRadius: '5px',
                cursor: 'pointer'
              }}
            >
              Reset
            </button>
          </div>
        </DropdownMenu>
      </Dropdown>
    </th>
  );
};

export default FilterDepDropdown;
