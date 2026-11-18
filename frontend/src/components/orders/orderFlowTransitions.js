export function getFlowFieldName(department) {
  const fieldName = department?.toLowerCase();
  if (department?.length !== 5) {
    return fieldName;
  }

  return fieldName.slice(0, 3) + fieldName.charAt(3).toUpperCase() + fieldName.slice(4);
}

export function getNextFlowStatus(currentStatus, flowValue, previousStatus, startsFromEmpty = false) {
  if (startsFromEmpty && currentStatus === "" && flowValue === "FWD") {
    return "R";
  }
  if (currentStatus === "R" && flowValue === "FWD") {
    return "Y";
  }
  if (currentStatus === "Y" && flowValue === "FWD") {
    return "G";
  }
  if (startsFromEmpty && currentStatus === "R" && flowValue === "RVS") {
    return "";
  }
  if (flowValue === "HLT") {
    return "B";
  }
  if (currentStatus === "B" && flowValue === "RVS") {
    return previousStatus;
  }

  return currentStatus;
}
