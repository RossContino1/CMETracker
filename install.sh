#!/usr/bin/env bash
set -euo pipefail

APP_NAME="CMETracker"
APP_ID="com.ross.cmetracker.CMETrackerApp"
ICON_NAME="caduceus-icon"
CATEGORIES="Office;Education;Science;"
COMMENT="CME tracking application"

INSTALL_DIR="${HOME}/Applications"
DESKTOP_DIR="${HOME}/.local/share/applications"
ICON_THEME_DIR="${HOME}/.local/share/icons/hicolor/256x256/apps"
PIXMAP_DIR="${HOME}/.local/share/pixmaps"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

APPIMAGE_SRC="$(ls -1 "${SCRIPT_DIR}"/CMETracker-*.AppImage 2>/dev/null | head -n 1 || true)"
if [[ -z "${APPIMAGE_SRC}" ]]; then
  APPIMAGE_SRC="$(ls -1 "${SCRIPT_DIR}"/*.AppImage 2>/dev/null | head -n 1 || true)"
fi

if [[ -z "${APPIMAGE_SRC}" ]]; then
  echo "ERROR: No .AppImage found next to install.sh"
  echo "Place install.sh in the same folder as CMETracker-*.AppImage"
  exit 1
fi

BUNDLED_ICON="${SCRIPT_DIR}/${ICON_NAME}.png"
[[ -f "${BUNDLED_ICON}" ]] || {
  echo "ERROR: ${ICON_NAME}.png not found next to install.sh"
  echo "Make sure you extracted the full CMETracker.zip contents"
  exit 1
}

mkdir -p "${INSTALL_DIR}" "${DESKTOP_DIR}" "${ICON_THEME_DIR}" "${PIXMAP_DIR}"

APPIMAGE_BASENAME="$(basename "${APPIMAGE_SRC}")"
APPIMAGE_DST="${INSTALL_DIR}/${APPIMAGE_BASENAME}"

echo "==> Removing older CMETracker AppImages"
rm -f "${INSTALL_DIR}/CMETracker-"*.AppImage 2>/dev/null || true

echo "==> Installing AppImage to: ${APPIMAGE_DST}"
cp -f "${APPIMAGE_SRC}" "${APPIMAGE_DST}"
chmod +x "${APPIMAGE_DST}"

echo "==> Installing icon"
cp -f "${BUNDLED_ICON}" "${ICON_THEME_DIR}/${ICON_NAME}.png"
cp -f "${BUNDLED_ICON}" "${PIXMAP_DIR}/${ICON_NAME}.png"
chmod 644 "${ICON_THEME_DIR}/${ICON_NAME}.png" 2>/dev/null || true
chmod 644 "${PIXMAP_DIR}/${ICON_NAME}.png" 2>/dev/null || true

DESKTOP_FILE="${DESKTOP_DIR}/${ICON_NAME}.desktop"
ICON_ABS="${PIXMAP_DIR}/${ICON_NAME}.png"

echo "==> Writing desktop launcher: ${DESKTOP_FILE}"
cat > "${DESKTOP_FILE}" <<EOF2
[Desktop Entry]
Type=Application
Name=${APP_NAME}
Comment=${COMMENT}
Exec=${APPIMAGE_DST} %U
Icon=${ICON_ABS}
Terminal=false
Categories=${CATEGORIES}
StartupWMClass=${APP_ID}
EOF2

chmod 644 "${DESKTOP_FILE}"

echo "==> Refreshing desktop caches (best effort)"
command -v update-desktop-database >/dev/null 2>&1 && update-desktop-database "${DESKTOP_DIR}" || true
command -v gtk-update-icon-cache >/dev/null 2>&1 && gtk-update-icon-cache -f -t "${HOME}/.local/share/icons/hicolor" >/dev/null 2>&1 || true
command -v xdg-desktop-menu >/dev/null 2>&1 && xdg-desktop-menu forceupdate || true
command -v kbuildsycoca5 >/dev/null 2>&1 && kbuildsycoca5 --noincremental || true
command -v kbuildsycoca6 >/dev/null 2>&1 && kbuildsycoca6 --noincremental || true

echo
echo "Installed!"
echo "• Launch from your application menu by searching: ${APP_NAME}"
echo "• If it doesn't appear immediately, log out and log back in"
