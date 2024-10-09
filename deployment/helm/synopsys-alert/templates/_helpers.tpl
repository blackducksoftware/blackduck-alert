{{/* vim: set filetype=mustache: */}}
{{/*
_helpers.tpl - create helper functions for templating here...
*/}}

{{- define "alert.encryptionSecretEnvirons" -}}
{{- if not (hasKey .Values.secretEnvirons "ALERT_ENCRYPTION_PASSWORD") }}
ALERT_ENCRYPTION_PASSWORD: {{ required "must provide --set alertEncryptionPassword=\"\"" .Values.alertEncryptionPassword | b64enc }}
{{- end }}
{{- if not (hasKey .Values.secretEnvirons "ALERT_ENCRYPTION_GLOBAL_SALT") }}
ALERT_ENCRYPTION_GLOBAL_SALT: {{ required "must provide --set alertEncryptionGlobalSalt=\"\"" .Values.alertEncryptionGlobalSalt | b64enc }}
{{- end }}
{{- end -}}

{{/*
Image pull secrets to pull the image
*/}}
{{- define "alert.imagePullSecrets" -}}
{{- if .Values.imagePullSecrets }}
imagePullSecrets:
{{- range .Values.imagePullSecrets }}
- name: {{ . }}
{{- end }}
{{- end }}
{{- end -}}

{{/*
Environs for Alert Config Map
*/}}
{{- define "alert.environs" -}}
{{- range $key, $value := .Values.environs }}
{{ $key }}: {{ $value | quote }}
{{- end }}
{{- end -}}

{{/*
Enable SSL for External Postgres
*/}}
{{- define "enablePostgresSSL" -}}
{{- if .Values.postgres.sslMode }}
ALERT_DB_SSL_MODE: {{ .Values.postgres.sslMode }}
{{- else -}}
ALERT_DB_SSL_MODE: "disable"
{{- end -}}
{{- end -}}
