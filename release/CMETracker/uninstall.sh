#!/usr/bin/env bash
set -euo pipefail

ICON_NAME="caduceus-icon"
INSTALL_DIR="${HOME}/Applications"
DESKTOP_DIR="${HOME}/.local/share/applications"
ICON_THEME_DIR="${HOME}/.local/share/icons/hicolor/256x256/apps"
PIXMAP_DIR="${HOME}/.local/share/pixmaps"

echo "==> Removing desktop launcher"
rm -f "${DESKTOP_DIR}/${ICON_NAME}.desktop"

echo "==> Removing icons"
rm -f "${ICON_THEME_DIR}/${ICON_NAME}.png"
rm -f "${PIXMAP_DIR}/${ICON_NAME}.png"

echo "==> Removing CMETracker AppImages from ${INSTALL_DIR}"
rm -f "${INSTALL_DIR}/CMETracker-"*.AppImage 2>/dev/null || true

echo "==> Refreshing desktop caches"
command -v update-desktop-database >/dev/null 2>&1 && update-desktop-database "${DESKTOP_DIR}" || true
command -v gtk-update-icon-cache >/dev/null 2>&1 && gtk-update-icon-cache -f -t "${HOME}/.local/share/icons/hicolor" >/dev/null 2>&1 || true
command -v xdg-desktop-menu >/dev/null 2>&1 && xdg-desktop-menu forceupdate || true
command -v kbuildsycoca5 >/dev/null 2>&1 && kbuildsycoca5 --noincremental || true
command -v kbuildsycoca6 >/dev/null 2>&1 && kbuildsycoca6 --noincremental || true

echo "Uninstalled."
