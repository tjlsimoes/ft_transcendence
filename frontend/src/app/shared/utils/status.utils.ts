export function statusClass(status: string): string {
switch(status) {
    case 'ONLINE':  return 'online';
    case 'IN_QUEUE':
    case 'IN_DUEL': return 'in-game';
    default:        return 'offline';
}
}