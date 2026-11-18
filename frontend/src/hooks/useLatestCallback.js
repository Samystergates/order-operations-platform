import { useEffect, useRef } from "react";

export default function useLatestCallback(callback) {
  const callbackRef = useRef(callback);

  useEffect(() => {
    callbackRef.current = callback;
  });

  return callbackRef;
}
