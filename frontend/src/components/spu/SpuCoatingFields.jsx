import React from "react";

const coatingOptions = [
  { id: "natLakken", label: "Nat Lakken", type: "radio", enabledFor: ["natLakken"] },
  { id: "poedercoaten", label: "Poedercoaten", type: "radio", enabledFor: ["poedercoaten"] },
  { id: "Stralen", label: "Stralen", enabledFor: ["natLakken"] },
  { id: "StralenGedeeltelijk", label: "Stralen Gedeeltelijk", enabledFor: ["natLakken"] },
  { id: "Schooperen", label: "Schooperen", enabledFor: ["natLakken"] },
  { id: "Kitten", label: "Kitten", enabledFor: ["poedercoaten"] },
  { id: "Primer", label: "Primer", enabledFor: ["natLakken"] },
  { id: "Ontlakken", label: "Ontlakken", enabledFor: ["poedercoaten"] },
  { id: "Aflakken", label: "Aflakken", enabledFor: ["natLakken", "poedercoaten"] },
  { id: "Blankelak", label: "Blanke lak", enabledFor: ["natLakken"] },
];

function CoatingOption({ option, selectedRadio, checkedStates, onChange }) {
  const isRadio = option.type === "radio";
  const disabled = !isRadio && !option.enabledFor.includes(selectedRadio);
  const checked = isRadio
    ? selectedRadio === option.id
    : checkedStates[option.id] === "JA";

  return (
    <div style={{ display: "flex", alignItems: "center", marginBottom: "15px" }}>
      <div style={{ marginRight: "10px", display: "flex", alignItems: "center" }}>
        <input
          type={isRadio ? "radio" : "checkbox"}
          id={option.id}
          name={isRadio ? "radioGroup" : undefined}
          style={{ marginRight: "7px", transform: "scale(1.7)" }}
          disabled={disabled}
          checked={checked}
          onChange={onChange}
          onClick={isRadio ? onChange : undefined}
        />
      </div>
      <div style={{ marginLeft: "10px" }}>
        <label
          htmlFor={option.id}
          style={{
            fontSize: "1.3rem",
            marginLeft: "7px",
            color: disabled ? "gray" : "black",
          }}
        >
          {option.label}
        </label>
      </div>
    </div>
  );
}

export default function SpuCoatingFields({
  selectedRadio,
  checkedStates,
  onChange,
}) {
  return (
    <div
      style={{
        height: "100%",
        padding: "10px",
        borderRadius: "5px",
        boxShadow: "0 2px 4px rgba(0, 0, 0, 0.1)",
        display: "flex",
        flexDirection: "column",
      }}
    >
      {coatingOptions.map((option) => (
        <CoatingOption
          key={option.id}
          option={option}
          selectedRadio={selectedRadio}
          checkedStates={checkedStates}
          onChange={onChange}
        />
      ))}
    </div>
  );
}
