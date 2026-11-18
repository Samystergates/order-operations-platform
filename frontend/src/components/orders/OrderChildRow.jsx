import React from "react";
import {
  Dropdown,
  DropdownItem,
  DropdownMenu,
  DropdownToggle,
} from "reactstrap";

export default function OrderChildRow({
  item,
  rowDropDownOpen,
  handleColClick,
  renderBackorderColumn,
  getChildColorDot,
  renderDropdownItems,
  stickers,
  printingSticker,
}) {
  return (
    <tr
      key={`${item.orderNumber},${item.regel}`}
      onClick={(e) => {
        handleColClick(item.regel);
        e.stopPropagation();
      }}
      title={item.tekst ? item.tekst : ""}
    >
      <td>{item.regel}</td>
      <td onClick={(e) => e.stopPropagation()}>
        {renderBackorderColumn(item.backOrder, item.orderNumber, item.id, "inner")}
      </td>
      <td>{getChildColorDot(item.orderNumber, item?.sme, item.id, "sme")}</td>
      <td>{getChildColorDot(item.orderNumber, item?.spu, item.id, "spu")}</td>
      <td>
        <Dropdown
          isOpen={rowDropDownOpen[item.regel]}
          toggle={() => {
            handleColClick(item.regel);
          }}
        >
          <DropdownToggle data-toggle="dropdown" tag="span">
            {item.aantal}
          </DropdownToggle>
          <DropdownMenu>
            <DropdownItem
              header
              style={{
                fontWeight: "bold",
                fontSize: "large",
              }}
              className="dropdown-header"
            >
              Order: {item.orderNumber}, {item.regel}
            </DropdownItem>
            {renderDropdownItems(
              "SME",
              item.id,
              item.orderNumber,
              `${item.orderNumber},${item.regel}`,
              item?.sme
            )}
            {renderDropdownItems(
              "SPU",
              item.id,
              item.orderNumber,
              `${item.orderNumber},${item.regel}`,
              item?.spu
            )}

            {stickers.some((stk) => stk.product === item.product) && (
              <DropdownItem
                className="DropDown-Size"
                onClick={() => printingSticker(item.product, item.orderNumber)}
              >
                <li
                  style={{
                    borderBottom: "2px solid #ccc",
                    padding: "8px 0",
                  }}
                >
                  Print Sticker
                </li>
              </DropdownItem>
            )}
          </DropdownMenu>
        </Dropdown>
      </td>
      <td>{item.product}</td>
      <td>{item.omsumin}</td>
      <td>{item.deliveryDate}</td>
      <td>{item.user}</td>
      <td>{getChildColorDot(item.orderNumber, item.monLb, item.id, "monLb")}</td>
      <td>{getChildColorDot(item.orderNumber, item.monTr, item.id, "monTr")}</td>
      <td>{getChildColorDot(item.orderNumber, item.mwe, item.id, "mwe")}</td>
      <td>{getChildColorDot(item.orderNumber, item.ser, item.id, "ser")}</td>
      <td>{getChildColorDot(item.orderNumber, item.tra, item.id, "tra")}</td>
      <td>{getChildColorDot(item.orderNumber, item.exp, item.id, "exp")}</td>
    </tr>
  );
}
