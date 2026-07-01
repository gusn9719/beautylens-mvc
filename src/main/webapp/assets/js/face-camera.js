(function (window) {
  const BL = window.BL || {};

  function createModal(title) {
    const modal = document.createElement("div");
    modal.className = "face-modal";
    modal.innerHTML = `
      <div class="face-dialog" role="dialog" aria-modal="true" aria-label="${BL.escape(title)}">
        <div class="section-head">
          <div>
            <h2>${BL.escape(title)}</h2>
            <p id="face-step-text" class="muted"></p>
          </div>
          <button id="face-close" class="btn btn-ghost" type="button">닫기</button>
        </div>
        <div class="face-progress" aria-label="얼굴 촬영 진행률">
          <span id="face-progress-bar" style="width:0%"></span>
        </div>
        <div id="face-progress-text" class="face-progress-text">준비 중</div>
        <p id="face-guide-text" class="face-guide-text">얼굴을 화면 중앙에 맞춰주세요.</p>
        <div class="face-video-wrap">
          <video id="face-video" class="face-video" autoplay playsinline muted></video>
          <div id="face-scan-ring" class="face-scan-ring" aria-hidden="true"></div>
          <div id="face-scan-status" class="face-scan-status">준비 중</div>
        </div>
        <canvas id="face-canvas" width="480" height="360" style="display:none;"></canvas>
        <div id="face-thumbs" class="face-thumbs"></div>
        <div id="face-camera-notice"></div>
        <div class="actions">
          <button id="face-capture" class="btn btn-primary" type="button">촬영</button>
        </div>
      </div>`;
    document.body.appendChild(modal);
    return modal;
  }

  function cameraMessage(error) {
    const name = error && error.name ? error.name : "";
    if (name === "NotAllowedError" || name === "PermissionDeniedError") {
      return "카메라 권한을 허용해 주세요.";
    }
    if (name === "NotFoundError" || name === "DevicesNotFoundError") {
      return "사용 가능한 카메라를 찾지 못했습니다.";
    }
    if (name === "NotReadableError" || name === "TrackStartError") {
      return "카메라를 다른 프로그램에서 사용 중일 수 있습니다.";
    }
    return "카메라를 시작하지 못했습니다. 다시 시도해 주세요.";
  }

  async function openStream(video) {
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      throw new Error("이 브라우저에서는 카메라를 사용할 수 없습니다.");
    }
    const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
    try {
      video.srcObject = stream;
      await video.play();
      return stream;
    } catch (e) {
      stopStream(stream);
      video.srcObject = null;
      throw e;
    }
  }

  function stopStream(stream) {
    if (!stream) return;
    stream.getTracks().forEach(function (track) { track.stop(); });
  }

  function capture(video, canvas) {
    const ctx = canvas.getContext("2d");
    const width = canvas.width;
    const height = canvas.height;
    ctx.drawImage(video, 0, 0, width, height);
    return canvas.toDataURL("image/jpeg", 0.9);
  }

  BL.faceCaptureSequence = function (steps, title) {
    return new Promise(async function (resolve, reject) {
      const modal = createModal(title || "얼굴 등록");
      const video = modal.querySelector("#face-video");
      const canvas = modal.querySelector("#face-canvas");
      const stepText = modal.querySelector("#face-step-text");
      const notice = modal.querySelector("#face-camera-notice");
      const thumbs = modal.querySelector("#face-thumbs");
      const captureBtn = modal.querySelector("#face-capture");
      const progressBar = modal.querySelector("#face-progress-bar");
      const progressText = modal.querySelector("#face-progress-text");
      const guideText = modal.querySelector("#face-guide-text");
      let stream = null;
      let index = 0;
      let canceled = false;
      let settled = false;
      let cleaned = false;
      const images = [];

      function cleanup() {
        if (cleaned) return;
        cleaned = true;
        stopStream(stream);
        stream = null;
        if (video.srcObject) video.srcObject = null;
        modal.remove();
      }

      function updateStep() {
        const total = steps.length || 1;
        const current = Math.min(index + 1, total);
        const percent = Math.round((index / total) * 100);
        stepText.textContent = steps[index] || "촬영이 완료되었습니다.";
        guideText.textContent = "얼굴이 잘 보이도록 밝은 곳에서 한 사람만 촬영해 주세요.";
        progressText.textContent = current + " / " + total;
        progressBar.style.width = percent + "%";
        captureBtn.textContent = current + "단계 촬영";
      }

      function finishProgress() {
        stepText.textContent = "얼굴 정보를 등록하고 있습니다.";
        guideText.textContent = "촬영한 얼굴 정보를 확인하는 중입니다. 잠시만 기다려 주세요.";
        progressText.textContent = steps.length + " / " + steps.length;
        progressBar.style.width = "100%";
        captureBtn.disabled = true;
        captureBtn.textContent = "처리 중";
      }

      modal.querySelector("#face-close").addEventListener("click", function () {
        if (settled) return;
        canceled = true;
        settled = true;
        cleanup();
        reject(new Error("촬영이 취소되었습니다."));
      });

      captureBtn.addEventListener("click", function () {
        if (canceled || settled) return;
        const image = capture(video, canvas);
        images.push(image);
        const img = document.createElement("img");
        img.src = image;
        img.alt = "촬영 이미지 " + images.length;
        thumbs.appendChild(img);
        index += 1;
        if (index >= steps.length) {
          finishProgress();
          window.setTimeout(function () {
            if (canceled || settled) return;
            settled = true;
            cleanup();
            resolve(images);
          }, 250);
          return;
        }
        updateStep();
      });

      try {
        stream = await openStream(video);
        if (canceled || settled) {
          stopStream(stream);
          stream = null;
          if (video.srcObject) video.srcObject = null;
          return;
        }
        updateStep();
      } catch (e) {
        if (canceled || settled) return;
        const message = e && e.message && e.message.indexOf("브라우저") >= 0 ? e.message : cameraMessage(e);
        const cameraError = new Error(message);
        captureBtn.disabled = true;
        settled = true;
        cleanup();
        reject(cameraError);
      }
    });
  };

  BL.faceCaptureLogin = function () {
    return new Promise(async function (resolve, reject) {
      const modal = createModal("얼굴로 로그인");
      const video = modal.querySelector("#face-video");
      const canvas = modal.querySelector("#face-canvas");
      const stepText = modal.querySelector("#face-step-text");
      const captureBtn = modal.querySelector("#face-capture");
      const progressBar = modal.querySelector("#face-progress-bar");
      const progressText = modal.querySelector("#face-progress-text");
      const guideText = modal.querySelector("#face-guide-text");
      const scanRing = modal.querySelector("#face-scan-ring");
      const scanStatus = modal.querySelector("#face-scan-status");
      let stream = null;
      let done = false;
      let resolved = false;
      let canceled = false;
      let cleaned = false;
      let cancelHandlers = [];
      let timers = [];

      function cleanup() {
        if (cleaned) return;
        cleaned = true;
        timers.forEach(function (timer) { window.clearTimeout(timer); window.clearInterval(timer); });
        timers = [];
        stopStream(stream);
        stream = null;
        if (video.srcObject) video.srcObject = null;
        modal.remove();
      }

      function setStatus(step, guide, progress, scan, busy) {
        stepText.textContent = step;
        guideText.textContent = guide;
        progressText.textContent = scan || step;
        progressBar.style.width = progress + "%";
        scanStatus.textContent = scan || step;
        scanRing.classList.remove("is-success", "is-error");
        scanRing.classList.toggle("is-active", !!busy);
      }

      function finishWithError(message) {
        if (done || canceled) return;
        done = true;
        setStatus("얼굴을 확인하지 못했습니다.", message, 100, "확인 실패", false);
        scanRing.classList.add("is-error");
        timers.push(window.setTimeout(function () {
          cleanup();
          reject(new Error(message));
        }, 900));
      }

      modal.querySelector("#face-close").addEventListener("click", function () {
        if (canceled) return;
        canceled = true;
        done = true;
        cancelHandlers.forEach(function (handler) {
          try { handler(); } catch (e) {}
        });
        cancelHandlers = [];
        cleanup();
        if (!resolved) {
          reject(new Error("촬영이 취소되었습니다."));
        }
      });

      function startAutoCapture() {
        let count = 3;
        setStatus("얼굴을 화면 중앙에 맞춰주세요.", "자동으로 얼굴 확인을 시작합니다. 잠시만 움직이지 말아 주세요.", 12, "자동 확인 준비 중", true);
        const timer = window.setInterval(function () {
          if (count > 0) {
            setStatus(count + "초 후 얼굴을 확인합니다.", "얼굴이 원 안에 들어오도록 화면 중앙을 바라봐 주세요.", 20 + ((3 - count) / 3 * 35), "자동 확인 준비 중", true);
            count -= 1;
            return;
          }
          window.clearInterval(timer);
          setStatus("얼굴 확인 중입니다.", "촬영한 얼굴 정보를 확인하고 있습니다.", 70, "얼굴 확인 중", true);
          const image = capture(video, canvas);
          stopStream(stream);
          stream = null;
          if (video.srcObject) video.srcObject = null;
          setStatus("로그인 정보 확인 중입니다.", "서버에서 얼굴 정보를 확인하고 있습니다. 잠시만 기다려 주세요.", 86, "로그인 정보 확인 중", true);
          resolved = true;
          resolve({
            image: image,
            setStatus: setStatus,
            complete: function () {
              if (done || canceled) return;
              done = true;
              setStatus("얼굴 확인이 완료되었습니다. 로그인 중입니다.", "잠시 후 이동합니다.", 100, "확인 완료", false);
              scanRing.classList.add("is-success");
            },
            fail: finishWithError,
            close: cleanup,
            isCanceled: function () { return canceled; },
            onCancel: function (handler) {
              if (typeof handler !== "function") return;
              if (canceled) {
                handler();
                return;
              }
              cancelHandlers.push(handler);
            }
          });
        }, 900);
        timers.push(timer);
      }

      try {
        modal.classList.add("face-login-auto");
        captureBtn.style.display = "none";
        stream = await openStream(video);
        if (canceled || done) {
          stopStream(stream);
          stream = null;
          if (video.srcObject) video.srcObject = null;
          return;
        }
        startAutoCapture();
      } catch (e) {
        if (canceled || done) return;
        const message = e && e.message && e.message.indexOf("브라우저") >= 0 ? e.message : cameraMessage(e);
        finishWithError(message);
      }
    });
  };

  window.addEventListener("pagehide", function () {
    document.querySelectorAll("video").forEach(function (video) {
      if (video.srcObject) stopStream(video.srcObject);
    });
  });

  window.BL = BL;
})(window);
