import React, { useEffect, useRef, useState } from "react";
import {
  Dropdown,
  DropdownItem,
  DropdownMenu,
  DropdownToggle,
} from "reactstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFilter } from "@fortawesome/free-solid-svg-icons";

const checkboxStyle = { width: "18px", height: "18px" };

function haveSameUsers(first, second) {
  return first.length === second.length
    && first.every((user) => second.includes(user));
}

export default function OrderUserFilterDropdown({
  uniqueUsers,
  selectedUsers,
  onSelectedUsersChange,
  onApply,
}) {
  const [isOpen, setIsOpen] = useState(false);
  const [tempSelectedUsers, setTempSelectedUsers] = useState(selectedUsers);
  const selectedUsersRef = useRef(selectedUsers);
  selectedUsersRef.current = selectedUsers;

  useEffect(() => {
    const currentSelectedUsers = selectedUsersRef.current;
    const hasSelectedUser = uniqueUsers.some((user) =>
      currentSelectedUsers.includes(user)
    );
    const nextSelectedUsers = hasSelectedUser
      ? [...currentSelectedUsers]
      : [...uniqueUsers];

    setTempSelectedUsers((current) =>
      haveSameUsers(current, nextSelectedUsers) ? current : nextSelectedUsers
    );
    if (!haveSameUsers(currentSelectedUsers, nextSelectedUsers)) {
      onSelectedUsersChange(nextSelectedUsers);
    }
  }, [uniqueUsers, onSelectedUsersChange]);

  const handleCheckboxChange = (event) => {
    const { value, checked } = event.target;
    if (value === "selectAll") {
      setTempSelectedUsers(checked ? [...uniqueUsers] : []);
      return;
    }

    setTempSelectedUsers((previous) =>
      checked ? [...previous, value] : previous.filter((user) => user !== value)
    );
  };

  const handleApply = () => {
    onApply(tempSelectedUsers);
    setIsOpen(false);
  };

  return (
    <th style={{ overflow: "hidden" }}>
      <Dropdown isOpen={isOpen} toggle={() => setIsOpen((open) => !open)}>
        <DropdownToggle data-toggle="dropdown" tag="span">
          Gebruiker (I) <FontAwesomeIcon icon={faFilter} style={{ marginTop: "5px" }} />
        </DropdownToggle>

        <DropdownMenu container="body" style={{ width: "220px", padding: "0" }}>
          <div style={{ maxHeight: "200px", overflowY: "auto", padding: "0.5rem" }}>
            <DropdownItem toggle={false}>
              <label>
                <input
                  type="checkbox"
                  style={checkboxStyle}
                  value="selectAll"
                  onChange={handleCheckboxChange}
                  checked={tempSelectedUsers.length === uniqueUsers.length}
                />{" "}
                - Select All/None
              </label>
            </DropdownItem>

            <div style={{ borderTop: "1px dashed #ccc", margin: "0.5rem 0" }} />

            {uniqueUsers.map((checkUser) => (
              <React.Fragment key={checkUser}>
                <DropdownItem toggle={false}>
                  <label>
                    <input
                      type="checkbox"
                      style={checkboxStyle}
                      value={checkUser}
                      onChange={handleCheckboxChange}
                      checked={tempSelectedUsers.includes(checkUser)}
                    />{" "}
                    - {checkUser}
                  </label>
                </DropdownItem>
                <div
                  style={{
                    borderTop: "1px dashed #ccc",
                    margin: "0.5rem 0",
                  }}
                />
              </React.Fragment>
            ))}
          </div>

          <div
            style={{
              borderTop: "1px solid #ccc",
              padding: "0.5rem",
              display: "flex",
              justifyContent: "space-between",
              background: "#f9f9f9",
              position: "sticky",
              bottom: 0,
            }}
          >
            <button
              onClick={() => setTempSelectedUsers([...uniqueUsers])}
              className="btn btn-sm btn-secondary"
            >
              Reset
            </button>
            <button onClick={handleApply} className="btn btn-sm btn-primary">
              Apply
            </button>
          </div>
        </DropdownMenu>
      </Dropdown>
    </th>
  );
}
