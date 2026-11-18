import React from "react";

const fieldRowStyle = {
  display: "flex",
  alignItems: "center",
  marginBottom: "5px",
};

const labelStyle = {
  marginRight: "10px",
  minWidth: "80px",
  fontSize: "0.8rem",
};

const inputStyle = {
  maxWidth: "150px",
  fontSize: "0.8rem",
};

const dimensionFields = [
  ["naafgat", "Naafgat:", "48px", "mm."],
  ["steek", "Steekcircel:", "48px", "mm."],
  ["aantalBoutgat", "Aantal boutgaten:", "19px"],
  ["verdlingBoutgaten", "Verdeling boutgaten:", "0px"],
  ["diameter", "Diameter boutgat:", "17px", "mm."],
  ["maatVerzinking", "Maat verzinking:", "27px", "mm."],
  ["et", "ET:", "48px", "mm."],
  ["afstandVV", "Afstand voorzijde:", "17px", "mm."],
  ["afstandVA", "Afstand achterzijde:", "7px", "mm."],
  ["dikte", "Dikte schijf:", "48px", "mm."],
];

function DimensionField({ field, label, marginLeft, unit, entry, onEntryChange }) {
  return (
    <div style={fieldRowStyle}>
      <label htmlFor={field} style={labelStyle}>
        <strong>{label}</strong>
      </label>
      <input
        type="text"
        id={field}
        style={{ ...inputStyle, marginLeft }}
        value={entry?.[field]}
        onChange={(e) => onEntryChange({ ...entry, [field]: e.target.value })}
      />
      {unit && (
        <label htmlFor={field} style={{ ...labelStyle, marginLeft: "10px" }}>
          <strong>{unit}</strong>
        </label>
      )}
    </div>
  );
}

export default function SmeFormFields({
  entry,
  setEntry,
  selectedTypeBoutgat,
  onTypeBoutgatChange,
}) {
  return (
    <div
      style={{
        flex: 1,
        padding: "10px",
        display: "flex",
        flexDirection: "column",
        backgroundColor: "#eeeeee",
        overflowX: "auto",
      }}
    >
      {dimensionFields.slice(0, 5).map(([field, label, marginLeft, unit]) => (
        <DimensionField
          key={field}
          field={field}
          label={label}
          marginLeft={marginLeft}
          unit={unit}
          entry={entry}
          onEntryChange={setEntry}
        />
      ))}

      <div style={fieldRowStyle}>
        <label htmlFor="typeBoutgat" style={labelStyle}>
          <strong>Uitvoering boutgat:</strong>
        </label>
        <select
          id="typeBoutgat"
          style={{
            width: "150px",
            maxWidth: "150px",
            fontSize: "0.8rem",
            marginLeft: "8px",
          }}
          value={selectedTypeBoutgat}
          onChange={onTypeBoutgatChange}
        >
          <option value=""></option>
          <option value="VLAK">VLAK</option>
          <option value="1XVZ">1xVZ</option>
          <option value="2XVZ">2xVZ</option>
        </select>
      </div>

      {dimensionFields.slice(5).map(([field, label, marginLeft, unit]) => (
        <DimensionField
          key={field}
          field={field}
          label={label}
          marginLeft={marginLeft}
          unit={unit}
          entry={entry}
          onEntryChange={setEntry}
        />
      ))}
    </div>
  );
}
