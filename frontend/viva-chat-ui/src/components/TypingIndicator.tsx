interface TypingIndicatorProps {
  users: string[];
}

const TypingIndicator = ({ users }: TypingIndicatorProps) => {
  if (users.length === 0) return null;

  const getText = () => {
    if (users.length === 1) {
      return `${users[0]} está escribiendo...`;
    }
    if (users.length === 2) {
      return `${users[0]} y ${users[1]} están escribiendo...`;
    }
    return `${users[0]} y ${users.length - 1} más están escribiendo...`;
  };

  return (
    <div className="flex items-center gap-2 px-3 py-2">
      <div className="flex gap-1">
        <div className="w-2 h-2 bg-primary rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
        <div className="w-2 h-2 bg-primary rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
        <div className="w-2 h-2 bg-primary rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
      </div>
      <p className="text-sm text-muted-foreground italic">{getText()}</p>
    </div>
  );
};

export default TypingIndicator;
