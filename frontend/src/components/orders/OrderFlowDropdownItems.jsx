import React from "react";
import { DropdownItem } from "reactstrap";
import { printMonExp, printSmeExp, printSpuExp, printSer } from "../PrintUtil";
import { getFlowFieldName } from "./orderFlowTransitions";

export default function OrderFlowDropdownItems({
  dep,
  oId,
  orderKey,
  user,
  orderX,
  initToggleFlow,
  flowForward,
  flowHalt,
  flowReverse,
}) {
  const key = orderKey;
    if (
      user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Verkoop" ||
      user.departmentsSet[0].depName === "Werkplaats" || user.departmentsSet[0].depName === "Smederij" || user.departmentsSet[0].depName === "Logistiek") {
      const orderDep = orderX.find((item) => item.id === oId);
      const statusCode = orderDep?.[getFlowFieldName(dep)];

      if (statusCode === "") {
        return (
          <>
            <DropdownItem
              className="DropDown-Size"
              onClick={() =>
                initToggleFlow(key, oId, dep, statusCode, flowForward.current)
              }
            >
              {dep === "SME" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Verkoop" ||
                user.departmentsSet[0].depName === "Werkplaats") && (
                  <li
                    style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                  >
                    Onderhoud Smederij
                  </li>
                )}
              {dep === "SPU" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Verkoop" ||
                user.departmentsSet[0].depName === "Werkplaats") && (
                  <li
                    style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                  >
                    Onderhoud Spuiterij
                  </li>
                )}
            </DropdownItem>
            <DropdownItem
              className="DropDown-Size"
              onClick={() =>
                initToggleFlow(key, oId, dep, statusCode, flowHalt.current)
              }
            >
              {dep === "SME" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Smederij") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Smederij Backorder
                </li>
              )}{" "}
              {dep === "SPU" && user.departmentsSet[0].depName === "ADMIN" && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Spuiterij Backorder
                </li>
              )}
              {(dep === "MONLB" || dep === "MONTR" || dep === "MWE") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Werkplaats") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  {dep} Backorder
                </li>
              )}

              {(dep === "SER" || dep === "EXP") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Logistiek") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  {dep} Backorder
                </li>
              )}
            </DropdownItem>
            {dep === "SME" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Smederij") && (
              <DropdownItem
                className="DropDown-Size"
                onClick={() =>
                  printSmeExp(key)
                }
              >
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Print Smederij
                </li>

              </DropdownItem>
            )}
            {dep === "SPU" && user.departmentsSet[0].depName === "ADMIN" && (
              <DropdownItem
                className="DropDown-Size"
                onClick={() =>
                  printSpuExp(key)
                }
              >

                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Print Spuiterij
                </li>

              </DropdownItem>
            )}
          </>
        );
      }
      if (statusCode === "R") {
        return (
          <>
            <DropdownItem
              className="DropDown-Size"
              onClick={() =>
                initToggleFlow(key, oId, dep, statusCode, flowForward.current)
              }
            >
              {dep === "SME" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Verkoop" || user.departmentsSet[0].depName === "Werkplaats"
                || user.departmentsSet[0].depName === "Smederij") && (
                  <li
                    style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                  >
                    Start/Wijzigen Smederij
                  </li>
                )}{" "}
              {dep === "SPU" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Werkplaats" || user.departmentsSet[0].depName === "Verkoop") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Start/Wijzigen Spuiterij
                </li>
              )}
              {(dep === "MONLB" || dep === "MONTR" || dep === "MWE") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Werkplaats") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Start {dep}
                </li>
              )}

              {(dep === "SER") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Logistiek") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Gegevens Service
                </li>
              )}

              {(dep === "EXP") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Logistiek") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Start {dep}
                </li>
              )}
            </DropdownItem>
            <DropdownItem
              className="DropDown-Size"
              onClick={() =>
                initToggleFlow(key, oId, dep, statusCode, flowHalt.current)
              }
            >
              {dep === "SME" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Smederij") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Smederij Backorder
                </li>
              )}{" "}
              {dep === "SPU" && user.departmentsSet[0].depName === "ADMIN" && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Spuiterij Backorder
                </li>
              )}
              {(dep === "MONLB" || dep === "MONTR" || dep === "MWE") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Werkplaats") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  {dep} Backorder
                </li>
              )}

              {(dep === "SER" || dep === "EXP") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Logistiek") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  {dep} Backorder
                </li>
              )}
            </DropdownItem>
            {dep === "SME" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Smederij") && (
              <DropdownItem
                className="DropDown-Size"
                onClick={() =>
                  printSmeExp(key)
                }
              >
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Print Smederij
                </li>

              </DropdownItem>
            )}
            {dep === "SPU" && user.departmentsSet[0].depName === "ADMIN" && (
              <DropdownItem
                className="DropDown-Size"
                onClick={() =>
                  printSpuExp(key)
                }
              >

                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Print Spuiterij
                </li>

              </DropdownItem>
            )}
          </>
        );
      }
      if (statusCode === "Y") {
        return (
          <>
            <DropdownItem
              className="DropDown-Size"
              onClick={() =>
                initToggleFlow(key, oId, dep, statusCode, flowForward.current)
              }
            >
              {dep === "SME" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Smederij") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Afmelden Smederij
                </li>
              )}{" "}
              {dep === "SPU" && user.departmentsSet[0].depName === "ADMIN" && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Afmelden Spuiterij
                </li>
              )}
              {(dep === "MONLB" || dep === "MONTR" || dep === "MWE") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Werkplaats") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Afmelden {dep}
                </li>
              )}
              {(dep === "SER") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Logistiek") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Gegevens Service
                </li>
              )}

              {(dep === "EXP") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Logistiek") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Afmelden {dep}
                </li>
              )}
            </DropdownItem>
            <DropdownItem
              className="DropDown-Size"
              onClick={() =>
                initToggleFlow(key, oId, dep, statusCode, flowHalt.current)
              }
            >
              {dep === "SME" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Smederij") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Smederij Backorder
                </li>
              )}{" "}
              {dep === "SPU" && user.departmentsSet[0].depName === "ADMIN" && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Spuiterij Backorder
                </li>
              )}
              {(dep === "MONLB" || dep === "MONTR" || dep === "MWE") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Werkplaats") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  {dep} Backorder
                </li>
              )}

              {(dep === "SER" || dep === "EXP") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Logistiek") && (
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  {dep} Backorder
                </li>
              )}
            </DropdownItem>
            {dep === "SME" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Smederij") && (
              <DropdownItem
                className="DropDown-Size"
                onClick={() =>
                  printSmeExp(key)
                }
              >
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Print Smederij
                </li>

              </DropdownItem>
            )}
            {dep === "SPU" && user.departmentsSet[0].depName === "ADMIN" && (
              <DropdownItem
                className="DropDown-Size"
                onClick={() =>
                  printSpuExp(key)
                }
              >
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Print Spuiterij
                </li>

              </DropdownItem>
            )}
          </>
        );
      }
      if (statusCode === "G") {
        return (
          <>
            <DropdownItem
              className="DropDown-Size"
              style={{ color: "#ccc", pointerEvents: "none" }}
            >
              <li
                style={{
                  pointerEvents: "none",
                  borderBottom: "2px solid #ccc",
                  padding: "8px 0",
                }}
              >
                Completed {dep}
              </li>
            </DropdownItem>
            {dep === "SME" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Smederij") && (
              <DropdownItem
                className="DropDown-Size"
                onClick={() =>
                  printSmeExp(key)
                }
              >
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Print Smederij
                </li>

              </DropdownItem>
            )}
            {dep === "SPU" && user.departmentsSet[0].depName === "ADMIN" && (
              <DropdownItem
                className="DropDown-Size"
                onClick={() =>
                  printSpuExp(key)
                }
              >

                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Print Spuiterij
                </li>

              </DropdownItem>
            )}
          </>
        );
      }
      if (statusCode === "B") {
        return (
          <>
            {dep === "SME" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Smederij") && (
              <DropdownItem
                className="DropDown-Size"
                onClick={() =>
                  initToggleFlow(key, oId, dep, statusCode, flowReverse.current)
                }
              >
                <li style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}>
                  Mark {dep} Available
                </li>
              </DropdownItem>
            )}

            {dep === "SPU" && user.departmentsSet[0].depName === "ADMIN" && (
              <DropdownItem
                className="DropDown-Size"
                onClick={() =>
                  initToggleFlow(key, oId, dep, statusCode, flowReverse.current)
                }
              >
                <li style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}>
                  Mark {dep} Available
                </li>
              </DropdownItem>
            )}

            {(dep === "MONLB" || dep === "MONTR" || dep === "MWE") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Werkplaats") && (
              <DropdownItem
                className="DropDown-Size"
                onClick={() =>
                  initToggleFlow(key, oId, dep, statusCode, flowReverse.current)
                }
              >
                <li style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}>
                  Mark {dep} Available
                </li>
              </DropdownItem>
            )}

            {(dep === "SER" || dep === "EXP") && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Logistiek") && (
              <DropdownItem
                className="DropDown-Size"
                onClick={() =>
                  initToggleFlow(key, oId, dep, statusCode, flowReverse.current)
                }
              >
                <li style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}>
                  Mark {dep} Available
                </li>
              </DropdownItem>
            )}

            {dep === "SME" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Smederij") && (
              <DropdownItem
                className="DropDown-Size"
                onClick={() =>
                  printSmeExp(key)
                }
              >
                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Print Smederij
                </li>

              </DropdownItem>
            )}
            {dep === "SPU" && user.departmentsSet[0].depName === "ADMIN" && (
              <DropdownItem
                className="DropDown-Size"
                onClick={() =>
                  printSpuExp(key)
                }
              >

                <li
                  style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}
                >
                  Print Spuiterij
                </li>

              </DropdownItem>
            )}
          </>
        );
      }

      if (dep === "MONLB" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Werkplaats")) {
        return (
          <>
            <DropdownItem
              className="DropDown-Size"
              onClick={() =>
                initToggleFlow(key, oId, dep, statusCode, flowReverse.current)
              }
            >
              <li style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}>
                Mark {dep} Available
              </li>
            </DropdownItem>
          </>
        );
      }


      if (dep === "MON-PRINT" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Werkplaats")) {
        const orderNumberMonPrint = key.split(",")[0];
        return (
          <>
            <DropdownItem
              className="DropDown-Size"
              onClick={() =>
                printMonExp(orderNumberMonPrint)
              }
            >
              <li style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}>
                {dep}
              </li>
            </DropdownItem>
          </>
        );
      }

      if (dep === "SER-PRINT" && (user.departmentsSet[0].depName === "ADMIN" || user.departmentsSet[0].depName === "Logistiek")) {
        const orderNumberSerPrint = key.split(",")[0];
        return (
          <>
            <DropdownItem
              className="DropDown-Size"
              onClick={() =>
                printSer(orderNumberSerPrint)
              }
            >
              <li style={{ borderBottom: "2px solid #ccc", padding: "8px 0" }}>
                {dep}
              </li>
            </DropdownItem>
          </>
        );
      }
    }
    return null;

}
