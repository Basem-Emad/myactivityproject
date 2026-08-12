import type { CSSProperties } from "react";

import "./LoadingSkeleton.css";

interface LoadingSkeletonProps {
  width?: CSSProperties["width"];
  height?: CSSProperties["height"];
  className?: string;
}

export default function LoadingSkeleton({
  width = "100%",
  height = "16px",
  className = "",
}: LoadingSkeletonProps) {
  return (
    <span
      className={`loading-skeleton ${className}`.trim()}
      style={{ width, height }}
      aria-hidden="true"
    />
  );
}