import React from "react";
import {
  Dropdown,
  DropdownItem,
  DropdownMenu,
  DropdownToggle,
  Tooltip,
} from "reactstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faAdd,
  faExclamationTriangle,
  faSubtract,
} from "@fortawesome/free-solid-svg-icons";

function OrderWorkflowMenu({
  item,
  user,
  flowReverse,
  renderDropdownItems,
  orderDepON,
  toggleNoteModal,
  toggleBOFlow,
}) {
  const key = `${item.orderNumber},${item.regel}`;
  const canManageException =
    user?.departmentsSet?.[0]?.depName === "ADMIN" ||
    user?.departmentsSet?.[0]?.depName === "EXCBO";
  const orderDepartment = orderDepON(item.orderNumber);

  return (
    <>
      {item.orderType === "LAS" && renderDropdownItems("EXP", item.id, item.orderNumber, key, item.exp)}
      {(item.orderType === "LSO" || item.orderType === "LSL" || item.orderType === "LPL" || item.orderType === "LSP") &&
        renderDropdownItems("SER", item.id, item.orderNumber, key, item.ser)}

      {item.orderType === "MAO" && (
        <>
          {renderDropdownItems("MONLB", item.id, item.orderNumber, key, item.monLb)}
          {renderDropdownItems("EXP", item.id, item.orderNumber, key, item.exp)}
        </>
      )}

      {(item.orderType === "MLO" || item.orderType === "MLP") && (
        <>{renderDropdownItems("MONLB", item.id, item.orderNumber, key, item.monLb)}</>
      )}

      {(item.orderType === "MWO" || item.orderType === "MWP") && (
        <>{renderDropdownItems("MWE", item.id, item.orderNumber, key, item.mwe)}</>
      )}

      {(item.orderType === "MSO" || item.orderType === "MSP") && (
        <>
          {renderDropdownItems("MONLB", item.id, item.orderNumber, key, item.monLb)}
          {renderDropdownItems("SER", item.id, item.orderNumber, key, item.ser)}
        </>
      )}

      {(item.orderType === "MLT" || item.orderType === "MLE") && (
        <>{renderDropdownItems("MONTR", item.id, item.orderNumber, key, item.monTr)}</>
      )}

      {item.orderType === "MST" && (
        <>
          {renderDropdownItems("MONTR", item.id, item.orderNumber, key, item.monTr)}
          {renderDropdownItems("SER", item.id, item.orderNumber, key, item.ser)}
        </>
      )}

      {item.orderType === "LAP" && (
        <>{renderDropdownItems("EXP", item.id, item.orderNumber, key, item.exp)}</>
      )}

      {item.orderType === "MAP" && (
        <>
          {renderDropdownItems("MONLB", item.id, item.orderNumber, key, item.monLb)}
          {renderDropdownItems("EXP", item.id, item.orderNumber, key, item.exp)}
        </>
      )}

      {[
        "MSP",
        "MLP",
        "MAP",
        "MSO",
        "MAO",
        "MLO",
        "MWO",
        "MWP",
        "MSE",
        "MLE",
        "MST",
        "MLT",
      ].includes(item.orderType) && (
        <>{renderDropdownItems("MON-PRINT", item.id, item.orderNumber, key, item.mwe)}</>
      )}

      {[
        "MSE",
        "MSP",
        "LSP",
        "MST",
        "MSO",
        "LPL",
        "LSL",
        "LSO",
      ].includes(item.orderType) && (
        <>{renderDropdownItems("SER-PRINT", item.id, item.orderNumber, key, item.mwe)}</>
      )}

      {item.orderType === "MSE" && (
        <>
          {renderDropdownItems("MONTR", item.id, item.orderNumber, key, item.monTr)}
          {renderDropdownItems("SER", item.id, item.orderNumber, key, item.ser)}
        </>
      )}

      {(orderDepartment?.exclamation === "" ||
        orderDepartment?.exclamation === null ||
        orderDepartment?.exclamation === "NEE") &&
        canManageException && (
          <DropdownItem
            className="DropDown-Size"
            onClick={() => toggleNoteModal(item.orderNumber, item.regel)}
          >
            <li
              style={{
                borderBottom: "2px solid #ccc",
                padding: "8px 0",
              }}
            >
              Add Note
            </li>
          </DropdownItem>
        )}

      {orderDepartment?.exclamation === "JA" && canManageException && (
        <DropdownItem
          className="DropDown-Size"
          onClick={() =>
            toggleBOFlow(item.orderNumber, item.regel, "EXCBO", "NEE", flowReverse)
          }
        >
          <li
            style={{
              borderBottom: "2px solid #ccc",
              padding: "8px 0",
            }}
          >
            Remove Note
          </li>
        </DropdownItem>
      )}
    </>
  );
}

export default function OrderRow({
  item,
  isExpanded,
  rowMainDropDownOpen,
  user,
  orderX,
  tooltipOpen,
  flowReverse,
  handleColClickMain,
  handleRowClick,
  renderBackorderColumn,
  getColorDot,
  renderDropdownItems,
  orderDepON,
  toggleNoteModal,
  toggleBOFlow,
  toggleTooltip,
}) {
  const currentOrder = orderX.find(
    (entry) => entry.orderNumber === item.orderNumber && entry.regel === item.regel
  );
  const tooltipKey = `${item.orderNumber}-${item.regel}`;

  return (
    <tr
      key={`${item.orderNumber},${item.regel}`}
      onClick={(e) => {
        handleColClickMain(item.orderNumber);
        e.stopPropagation();
      }}
      title={item.tekst ? item.tekst : ""}
    >
      <td
        style={{ color: "light" }}
        onClick={(e) => {
          handleRowClick(item.orderNumber);
          e.stopPropagation();
        }}
      >
        {isExpanded ? (
          <FontAwesomeIcon icon={faSubtract} />
        ) : (
          <FontAwesomeIcon icon={faAdd} />
        )}
      </td>
      <td>{item.orderNumber}</td>
      <td>{item.orderType}</td>
      <td onClick={(e) => e.stopPropagation()}>
        {renderBackorderColumn(item.backOrder, item.orderNumber, item.id, "outter")}
      </td>
      <td>{getColorDot(item.orderNumber, item?.sme, item.id, "sme")}</td>
      <td>{getColorDot(item.orderNumber, item?.spu, item.id, "spu")}</td>
      <td>
        <Dropdown
          isOpen={rowMainDropDownOpen[item.orderNumber]}
          toggle={() => {
            handleColClickMain(item.orderNumber);
          }}
        >
          <DropdownToggle data-toggle="dropdown" tag="span">
            {getColorDot(item.orderNumber, item.monLb, item.id, "monLb")}
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
              Order: {item.orderNumber}
            </DropdownItem>
            <OrderWorkflowMenu
              item={item}
              user={user}
              flowReverse={flowReverse}
              renderDropdownItems={renderDropdownItems}
              orderDepON={orderDepON}
              toggleNoteModal={toggleNoteModal}
              toggleBOFlow={toggleBOFlow}
            />
          </DropdownMenu>
        </Dropdown>
      </td>
      <td>{getColorDot(item.orderNumber, item.monTr, item.id, "monTr")}</td>
      <td>{getColorDot(item.orderNumber, item.mwe, item.id, "mwe")}</td>
      <td>{getColorDot(item.orderNumber, item.ser, item.id, "ser")}</td>
      <td>{getColorDot(item.orderNumber, item.tra, item.id, "tra")}</td>
      <td>{getColorDot(item.orderNumber, item.exp, item.id, "exp")}</td>
      <td>
        {currentOrder?.exclamation === "JA" && (
          <div
            style={{ color: "yellow", display: "inline-block" }}
            id={`icon-${tooltipKey}`}
          >
            <FontAwesomeIcon
              icon={faExclamationTriangle}
              style={{
                fontSize: "1.3em",
                color: "black",
              }}
              onMouseOver={() => toggleTooltip(item.orderNumber, item.regel)}
              onMouseOut={() => toggleTooltip(item.orderNumber, item.regel)}
            />
            <Tooltip
              placement="bottom"
              isOpen={tooltipOpen[tooltipKey]}
              target={`icon-${tooltipKey}`}
              toggle={() => toggleTooltip(item.orderNumber, item.regel)}
            >
              {currentOrder.excNote}
            </Tooltip>
          </div>
        )}
      </td>
      <td>{item.user}</td>
      <td>{item.organization}</td>
      <td>{item.customerName}</td>
      <td>{item.postCode}</td>
      <td>{item.city}</td>
      <td>{item.country}</td>
      <td>{item.deliveryDate}</td>
      <td>{item.referenceInfo}</td>
      <td>{item.creationDate}</td>
      <td>{item.verifierUser}</td>
    </tr>
  );
}
