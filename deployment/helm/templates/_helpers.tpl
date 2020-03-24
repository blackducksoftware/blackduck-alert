{{/* vim: set filetype=mustache: */}}
{{/*
_helpers.tpl - create helper functions for templating here...
*/}}

{{- define "alert.encryptionSecretEnvirons" -}}
{{ if not (hasKey .Values.secretEnvirons "ALERT_ENCRYPTION_PASSWORD") -}}
ALERT_ENCRYPTION_PASSWORD: {{ required "must provide --set alertEncryptionPassword=\"\"" .Values.alertEncryptionPassword | b64enc }}
{{- end }}
{{ if not (hasKey .Values.secretEnvirons "ALERT_ENCRYPTION_GLOBAL_SALT") -}}
ALERT_ENCRYPTION_GLOBAL_SALT: {{ required "must provide --set alertEncryptionGlobalSalt=\"\"" .Values.alertEncryptionGlobalSalt | b64enc }}
{{- end }}
{{- end -}}

{{/*
Image pull secrets to pull the image
*/}}
{{- define "alert.imagePullSecrets" -}}
{{- with .Values.imagePullSecrets -}}
imagePullSecrets:
{{- toYaml . | nindent 0 -}}
{{- end -}}
{{- end -}}