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
{{- if and (eq .Values.postgres.isExternal true) (eq .Values.postgres.ssl true) -}}
{{- if (eq .Values.postgres.sslUseFiles true) -}}
ALERT_DB_SSL_MODE: "verify-ca"
{{- else -}}
ALERT_DB_SSL_MODE: "require"
{{- end -}}
{{- else -}}
ALERT_DB_SSL_MODE: "disable"
{{- end -}}
{{- end -}}
