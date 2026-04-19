#!/usr/bin/env bash
set -euo pipefail

APP_NAME="CMETracker"
VERSION="1.0.0"
APP_ID="com.ross.cmetracker.CMETrackerApp"
ICON_NAME="caduceus-icon"
COMMENT="CME tracking application"
CATEGORIES="Office;Education;Science;"

RELEASE_ROOT="release"
RELEASE_DIR="${RELEASE_ROOT}/${APP_NAME}"

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APPDIR="${PROJECT_DIR}/${APP_NAME}.AppDir"
LINUXDEPLOY="${PROJECT_DIR}/linuxdeploy-x86_64.AppImage"

INPUT_JAR="${PROJECT_DIR}/CMETracker.jar"
APPIMAGE_NAME="${APP_NAME}-${VERSION}.AppImage"
ZIP_NAME="${APP_NAME}.zip"

ICON_CANDIDATES=(
  "${PROJECT_DIR}/out/cme/resources/${ICON_NAME}.png"
  "${PROJECT_DIR}/out/resources/${ICON_NAME}.png"
  "${PROJECT_DIR}/src/cme/resources/${ICON_NAME}.png"
  "${PROJECT_DIR}/cme/resources/${ICON_NAME}.png"
)

ICON_SRC=""
for candidate in "${ICON_CANDIDATES[@]}"; do
  if [[ -f "${candidate}" ]]; then
    ICON_SRC="${candidate}"
    break
  fi
done

echo "==> Cleaning old build"
rm -rf "${APPDIR}" "${RELEASE_ROOT}"
mkdir -p "${APPDIR}/usr/bin"
mkdir -p "${APPDIR}/usr/share/applications"
mkdir -p "${APPDIR}/usr/share/icons/hicolor/256x256/apps"
mkdir -p "${RELEASE_DIR}"

[[ -f "${INPUT_JAR}" ]] || { echo "ERROR: Missing CMETracker.jar"; exit 1; }
[[ -f "${LINUXDEPLOY}" ]] || { echo "ERROR: Missing linuxdeploy-x86_64.AppImage"; exit 1; }
[[ -n "${ICON_SRC}" ]] || {
  echo "ERROR: Could not find ${ICON_NAME}.png"
  echo "Looked in:"
  for candidate in "${ICON_CANDIDATES[@]}"; do
    echo "  ${candidate}"
  done
  exit 1
}
[[ -f "${PROJECT_DIR}/install.sh" ]] || { echo "ERROR: Missing install.sh"; exit 1; }
[[ -f "${PROJECT_DIR}/uninstall.sh" ]] || { echo "ERROR: Missing uninstall.sh"; exit 1; }
[[ -f "${PROJECT_DIR}/README.txt" ]] || { echo "ERROR: Missing README.txt"; exit 1; }
[[ -f "${PROJECT_DIR}/LICENSE.txt" ]] || { echo "ERROR: Missing LICENSE.txt"; exit 1; }

chmod +x "${LINUXDEPLOY}"

echo "==> Using icon: ${ICON_SRC}"

cp "${INPUT_JAR}" "${APPDIR}/usr/bin/CMETracker.jar"
cp "${ICON_SRC}" "${APPDIR}/${ICON_NAME}.png"
cp "${ICON_SRC}" "${APPDIR}/.DirIcon"
cp "${ICON_SRC}" "${APPDIR}/usr/share/icons/hicolor/256x256/apps/${ICON_NAME}.png"

cat > "${APPDIR}/${APP_NAME}.desktop" <<EOF2
[Desktop Entry]
Type=Application
Name=${APP_NAME}
Exec=AppRun
Icon=${ICON_NAME}
Terminal=false
Categories=${CATEGORIES}
Comment=${COMMENT}
StartupWMClass=${APP_ID}
EOF2

cp "${APPDIR}/${APP_NAME}.desktop" "${APPDIR}/usr/share/applications/${APP_NAME}.desktop"

cat > "${APPDIR}/AppRun" <<'EOF2'
#!/usr/bin/env bash
HERE="$(dirname "$(readlink -f "$0")")"
exec java -jar "$HERE/usr/bin/CMETracker.jar" "$@"
EOF2
chmod +x "${APPDIR}/AppRun"

echo "==> Building AppImage"
(
  cd "${PROJECT_DIR}"
  ARCH=x86_64 ./linuxdeploy-x86_64.AppImage \
    --appdir "${APPDIR}" \
    --desktop-file "${APPDIR}/usr/share/applications/${APP_NAME}.desktop" \
    --output appimage
)

FOUND_APPIMAGE="$(find "${PROJECT_DIR}" -maxdepth 1 -name '*.AppImage' ! -name 'linuxdeploy-*.AppImage' | head -n 1)"
[[ -n "${FOUND_APPIMAGE}" ]] || { echo "ERROR: AppImage build failed"; exit 1; }

mv "${FOUND_APPIMAGE}" "${RELEASE_DIR}/${APPIMAGE_NAME}"

cp "${PROJECT_DIR}/install.sh" "${RELEASE_DIR}/install.sh"
cp "${PROJECT_DIR}/uninstall.sh" "${RELEASE_DIR}/uninstall.sh"
cp "${PROJECT_DIR}/README.txt" "${RELEASE_DIR}/README.txt"
cp "${PROJECT_DIR}/LICENSE.txt" "${RELEASE_DIR}/LICENSE.txt"
cp "${ICON_SRC}" "${RELEASE_DIR}/${ICON_NAME}.png"

chmod +x "${RELEASE_DIR}/install.sh" "${RELEASE_DIR}/uninstall.sh"

echo "==> Creating zip package"
(
  cd "${RELEASE_ROOT}"
  zip -r "${ZIP_NAME}" "${APP_NAME}"
)

echo
echo "✅ Release complete:"
echo "  ${RELEASE_ROOT}/${ZIP_NAME}"
echo "  Folder inside zip: ${APP_NAME}/"
