/**
 * Converts total minutes into a human-readable label like "60h 0m"
 */
export function formatMinutesToLabel(totalMinutes: number): string {
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    return `${hours}h ${minutes}m`;
}
