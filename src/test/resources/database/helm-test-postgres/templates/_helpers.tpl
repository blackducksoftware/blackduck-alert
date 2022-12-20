{{/* vim: set filetype=mustache: */}}
{{/*
_helpers.tpl - create helper functions for templating here...
*/}}

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
