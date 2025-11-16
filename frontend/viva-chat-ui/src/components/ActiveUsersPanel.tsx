import { ScrollArea } from '@/components/ui/scroll-area';
import { Badge } from '@/components/ui/badge';
import { Users } from 'lucide-react';
import { cn } from '@/lib/utils';

export interface ActiveUserEntry {
  id?: number;
  name: string;
  isSelf?: boolean;
}

interface ActiveUsersPanelProps {
  users: ActiveUserEntry[];
  className?: string;
}

const ActiveUsersPanel = ({ users, className }: ActiveUsersPanelProps) => {
  const sorted = [...users].sort((a, b) => a.name.localeCompare(b.name));
  return (
    <div className={cn('h-full flex flex-col border-l border-border bg-card/50 backdrop-blur-sm', className)}>
      <div className="px-4 py-3 border-b flex items-center gap-2">
        <Users size={16} className="text-muted-foreground" />
        <h2 className="text-sm font-semibold">Usuarios ({sorted.length})</h2>
      </div>
      <ScrollArea className="flex-1 px-3 py-3">
        <ul className="space-y-1">
          {sorted.length === 0 && (
            <li className="text-xs text-muted-foreground">Nadie conectado aún</li>
          )}
          {sorted.map(u => (
            <li
              key={(u.id ?? u.name) + (u.isSelf ? '-self' : '')}
              className="flex items-center justify-between rounded-md px-2 py-1 text-sm bg-muted/40"
            >
              <span className="truncate">
                {u.name} {u.isSelf && <span className="text-primary font-medium">(Tú)</span>}
              </span>
              {u.isSelf && <Badge variant="secondary" className="text-[10px]">ACTIVO</Badge>}
            </li>
          ))}
        </ul>
      </ScrollArea>
    </div>
  );
};

export default ActiveUsersPanel;