import { MessageResponse, MessageType } from '@/types/api';
import { cn } from '@/lib/utils';
import { FileText, Download, Trash2 } from 'lucide-react';
import { API_BASE_URL } from '@/config/api';

interface MessageBubbleProps {
  message: MessageResponse;
  isOwn: boolean;
  onDelete?: (messageId: number) => void;
}

const MessageBubble = ({ message, isOwn, onDelete }: MessageBubbleProps) => {
  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  const isDeleted = message.isDeleted === true;

  const isImage = (mime?: string) => mime?.startsWith('image/');
  const isVideo = (mime?: string) => mime?.startsWith('video/');
  const isAudio = (mime?: string) => mime?.startsWith('audio/');
  const isPdf = (mime?: string, name?: string) => mime === 'application/pdf' || (name || '').toLowerCase().endsWith('.pdf');

  const getExtFromMime = (mime?: string): string | undefined => {
    if (!mime) return undefined;
    const map: Record<string, string> = {
      'application/pdf': 'pdf',
      'image/jpeg': 'jpg',
      'image/jpg': 'jpg',
      'image/png': 'png',
      'image/webp': 'webp',
      'image/gif': 'gif',
      'video/mp4': 'mp4',
      'video/webm': 'webm',
      'audio/mpeg': 'mp3',
      'audio/mp3': 'mp3',
      'audio/wav': 'wav',
      'application/zip': 'zip',
      'application/x-zip-compressed': 'zip',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'docx',
      'application/msword': 'doc',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'xlsx',
      'application/vnd.ms-excel': 'xls',
      'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'pptx',
      'application/vnd.ms-powerpoint': 'ppt',
    };
    return map[mime];
  };

  const getExtFromUrl = (url?: string): string | undefined => {
    if (!url) return undefined;
    try {
      const u = new URL(url);
      const pathname = u.pathname.split('?')[0];
      const last = pathname.split('/').pop() || '';
      const idx = last.lastIndexOf('.');
      if (idx > 0 && idx < last.length - 1) return last.slice(idx + 1);
    } catch {}
    return undefined;
  };

  const hasExtension = (name?: string) => !!name && /\.[A-Za-z0-9]+$/.test(name);

  const buildDownloadFilename = (att: MessageResponse['attachments'][number]) => {
    const original = att.originalFileName;
    const stored = att.fileName;
    const url = att.fileUrl;
    const mime = att.fileType;

    // Prefer original name if it already has extension
    if (hasExtension(original)) return original as string;

    // Use stored fileName if it has extension
    if (hasExtension(stored)) return stored as string;

    // Try to get extension from URL or MIME
    const urlExt = getExtFromUrl(url);
    const mimeExt = getExtFromMime(mime);
    const ext = urlExt || mimeExt;

    // Base name from original or stored or generic
    const base = (original && original !== 'file' ? original : stored) || 'file';
    const baseClean = base.replace(/\.[A-Za-z0-9]+$/, ''); // remove any trailing ext if present

    return ext ? `${baseClean}.${ext}` : baseClean;
  };

  // Cloudinary: do NOT add transformations (fl_attachment) to avoid 400 in strict-transformation accounts.
  // We'll rely on the <a download> attribute to set the filename client-side.
  const toCloudinaryDownloadUrl = (url: string, _filename?: string) => {
    return url;
  };

  const handleDownloadAttachment = (att: MessageResponse['attachments'][number]) => {
    // Siempre fuerza la descarga usando <a download> invisible
    const desiredName = buildDownloadFilename(att);
    // Use backend proxy to avoid CORS and enforce filename
    const base = API_BASE_URL.replace(/\/$/, '');
    const downloadUrl = `${base}/public/files/${att.id}/download`;
    const a = document.createElement('a');
    a.href = downloadUrl;
    a.download = desiredName || 'archivo';
    document.body.appendChild(a);
    a.click();
    a.remove();
  };

  return (
    <div className={cn('flex', isOwn ? 'justify-end' : 'justify-start')}>
      <div className={cn('max-w-[70%] space-y-1', isOwn ? 'items-end' : 'items-start')}>
        {!isOwn && (
          <p className="text-xs text-muted-foreground px-3">{message.senderNickname}</p>
        )}
        <div className="relative">
          <div
            className={cn(
              'rounded-2xl px-4 py-2',
              isOwn
                ? 'bg-chat-sent text-primary-foreground rounded-br-sm'
                : 'bg-chat-received text-foreground rounded-bl-sm'
            )}
          >
            {isDeleted ? (
              <p className="text-sm italic opacity-70">Mensaje eliminado</p>
            ) : (
              <>
                {message.content && (
                  <p className="text-sm break-words">{message.content}</p>
                )}

                {message.messageType === MessageType.FILE && message.attachments.length > 0 && (
                  <div className="mt-2 space-y-3">
                    {message.attachments.map((attachment) => {
                      const previewUrl = attachment.fileUrl;
                      const mime = attachment.fileType;
                      const name = attachment.originalFileName;

                      // Render preview based on MIME type
                      if (isImage(mime)) {
                        return (
                          <div key={attachment.id} className="space-y-2">
                            <img
                              src={previewUrl}
                              alt={name}
                              className="max-h-64 rounded-lg object-contain border border-border bg-background"
                              loading="lazy"
                            />
                            <div className="flex items-center justify-between gap-2 text-xs">
                              <span className="truncate">{name}</span>
                              <button
                                type="button"
                                onClick={() => handleDownloadAttachment(attachment)}
                                className="hover:opacity-80"
                                title="Descargar"
                                aria-label="Descargar"
                              >
                                <Download size={18} />
                              </button>
                            </div>
                          </div>
                        );
                      }

                      if (isVideo(mime)) {
                        return (
                          <div key={attachment.id} className="space-y-2">
                            <video
                              controls
                              className="max-h-72 w-full rounded-lg border border-border bg-black"
                            >
                              <source src={previewUrl} type={mime} />
                              Tu navegador no soporta la reproducción de video.
                            </video>
                            <div className="flex items-center justify-between gap-2 text-xs">
                              <span className="truncate">{name}</span>
                              <button
                                type="button"
                                onClick={() => handleDownloadAttachment(attachment)}
                                className="hover:opacity-80"
                                title="Descargar"
                                aria-label="Descargar"
                              >
                                <Download size={18} />
                              </button>
                            </div>
                          </div>
                        );
                      }

                      if (isAudio(mime)) {
                        return (
                          <div key={attachment.id} className="space-y-2">
                            <audio controls className="w-full">
                              <source src={previewUrl} type={mime} />
                              Tu navegador no soporta el audio HTML5.
                            </audio>
                            <div className="flex items-center justify-between gap-2 text-xs">
                              <span className="truncate">{name}</span>
                              <button
                                type="button"
                                onClick={() => handleDownloadAttachment(attachment)}
                                className="hover:opacity-80"
                                title="Descargar"
                                aria-label="Descargar"
                              >
                                <Download size={18} />
                              </button>
                            </div>
                          </div>
                        );
                      }

                      if (isPdf(mime, name)) {
                        return (
                          <div key={attachment.id} className="flex items-center gap-2 p-2 rounded-lg bg-muted">
                            <FileText size={24} />
                            <div className="flex-1 min-w-0">
                              <a
                                href={previewUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="text-sm font-medium underline truncate"
                                title={name}
                              >
                                {name}
                              </a>
                              <p className="text-xs opacity-70">{formatFileSize(attachment.fileSize)} · PDF</p>
                            </div>
                            <button
                              type="button"
                              onClick={() => handleDownloadAttachment(attachment)}
                              className="hover:opacity-80"
                              title="Descargar"
                              aria-label="Descargar"
                            >
                              <Download size={20} />
                            </button>
                          </div>
                        );
                      }

                      // Fallback generic file card
                      return (
                        <div
                          key={attachment.id}
                          className="flex items-center gap-2 p-2 rounded-lg bg-muted"
                        >
                          <FileText size={24} />
                          <div className="flex-1 min-w-0">
                            <a
                              href={previewUrl}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="text-sm font-medium underline truncate"
                              title={name}
                            >
                              {name}
                            </a>
                            <p className="text-xs opacity-70">{formatFileSize(attachment.fileSize)}</p>
                          </div>
                          <button
                            type="button"
                            onClick={() => handleDownloadAttachment(attachment)}
                            className="hover:opacity-80"
                            title="Descargar"
                            aria-label="Descargar"
                          >
                            <Download size={20} />
                          </button>
                        </div>
                      );
                    })}
                  </div>
                )}
              </>
            )}
          </div>
          {isOwn && !isDeleted && onDelete && (
            <button
              type="button"
              onClick={() => onDelete(message.id)}
              className={cn(
                'absolute -top-2',
                isOwn ? 'right-2' : 'left-2',
                'rounded-full p-1 bg-background/60 hover:bg-background/80 shadow'
              )}
              title="Eliminar mensaje"
              aria-label="Eliminar mensaje"
            >
              <Trash2 size={14} />
            </button>
          )}
        </div>
        <p className="text-xs text-muted-foreground px-3">{formatTime(message.sentAt)}</p>
      </div>
    </div>
  );
};

export default MessageBubble;
