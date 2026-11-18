import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPlus, faTrash } from "@fortawesome/free-solid-svg-icons";
import Select, { components } from "react-select";

const dropdownSelectStyles = {
  control: (provided, state) => ({
    ...provided,
    borderColor: state.isFocused ? "#0d6efd" : "#ced4da",
    boxShadow: state.isFocused
      ? "0 0 0 0.15rem rgba(13, 110, 253, 0.25)"
      : "none",
    minHeight: "32px",
    borderRadius: "10px",
    paddingLeft: "0.25rem",
    paddingRight: "0.25rem",
  }),
  valueContainer: (provided) => ({
    ...provided,
    padding: "0 0.4rem",
  }),
  singleValue: (provided) => ({
    ...provided,
    whiteSpace: "normal",
    lineHeight: 1.25,
    overflow: "visible",
    textOverflow: "clip",
  }),
  menu: (provided) => ({
    ...provided,
    zIndex: 12,
  }),
  menuList: (provided) => ({
    ...provided,
    padding: 0,
    maxHeight: "240px",
    overflowY: "auto",
  }),
  placeholder: (provided) => ({
    ...provided,
    color: "#6c757d",
  }),
};

const DropdownOption = (props) => {
  const { onDeleteOption } = props.selectProps;
  const preventDefaultAction = (event) => {
    event.preventDefault();
    event.stopPropagation();
  };

  const handleDelete = (event) => {
    preventDefaultAction(event);
    onDeleteOption?.(props.data);
  };

  return (
    <components.Option {...props}>
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          gap: "0.5rem",
        }}
      >
        <span style={{ flex: 1, minWidth: 0 }}>{props.label}</span>
        {onDeleteOption && !props.data?.isFooter && (
          <button
            type="button"
            tabIndex={-1}
            onMouseDown={preventDefaultAction}
            onClick={handleDelete}
            style={{
              border: "none",
              background: "transparent",
              padding: 0,
              cursor: "pointer",
            }}
            aria-label={`Delete ${props.label}`}
          >
            <FontAwesomeIcon icon={faTrash} color="#dc3545" />
          </button>
        )}
      </div>
    </components.Option>
  );
};

const DropdownMenuList = (props) => {
  const { addOptionLabel, onAddOption } = props.selectProps;

  const handleAdd = (event) => {
    event.preventDefault();
    event.stopPropagation();
    onAddOption?.();
  };

  return (
    <components.MenuList {...props}>
      {props.children}
      {onAddOption && (
        <div
          style={{
            position: "sticky",
            bottom: 0,
            background: "#f8f9fa",
            borderTop: "1px solid #e9ecef",
          }}
        >
          <button
            type="button"
            onMouseDown={handleAdd}
            style={{
              width: "100%",
              border: "none",
              background: "transparent",
              padding: "0.5rem 0.75rem",
              cursor: "pointer",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: "0.35rem",
            }}
          >
            <FontAwesomeIcon icon={faPlus} color="#198754" size="sm" />
            <span style={{ fontWeight: 600 }}>{addOptionLabel}</span>
          </button>
        </div>
      )}
    </components.MenuList>
  );
};

const dropdownComponents = {
  Option: DropdownOption,
  MenuList: DropdownMenuList,
};

export const mapOptions = (items, getLabel) =>
  (items || []).map((item) => ({
    value: getLabel(item),
    label: getLabel(item),
    raw: item,
  }));

export const mapTruckOptions = (items) =>
  (items || []).map((item) => {
    const label = `${item.truck} , ${item.truckType}`;
    return {
      value: label,
      label,
      raw: item,
    };
  });

export const getSelectedOption = (options, currentValue) => {
  if (!currentValue) {
    return null;
  }

  return (
    options.find((option) => option.value === currentValue) || {
      value: currentValue,
      label: currentValue,
    }
  );
};

const DropdownField = ({
  id,
  label,
  placeholder,
  value,
  options,
  onChange,
  onDeleteOption,
  onAddOption,
  selectWidth = "100%",
}) => (
  <div
    style={{
      width: "100%",
      display: "flex",
      flexDirection: "column",
      gap: "0.25rem",
      padding: "0.2rem 0.25rem",
    }}
  >
    <label
      htmlFor={id}
      style={{
        marginBottom: 0,
        color: "#212529",
        fontWeight: 600,
        fontSize: "1rem",
        lineHeight: 1.3,
      }}
    >
      {`${label}:`}
    </label>
    <div style={{ width: selectWidth, minWidth: 0 }}>
      <Select
        inputId={id}
        placeholder={placeholder}
        value={value}
        options={options}
        isClearable
        styles={dropdownSelectStyles}
        components={dropdownComponents}
        onChange={onChange}
        addOptionLabel={`Add ${label}`}
        onAddOption={onAddOption}
        onDeleteOption={onDeleteOption}
        classNamePrefix="transport-select"
      />
    </div>
  </div>
);

export default DropdownField;
