package com.yisy.modules.doraneuron.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.model.multigame.GameProfile;
import com.yisy.modules.doraneuron.service.multigame.GameRegistryService;
import com.yisy.modules.doraneuron.service.multigame.GameRouterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Public-facing web controller that serves the landing page and proxies or
 * redirects requests to the actual DoraMCU game server.
 *
 * This is the entry point that visitors see — the Neuron acts as a gateway
 * between the public internet and the game server.
 *
 * Phase 14: Supports multi-game routing via /game/{gameId}/** paths.
 */
@RestController
@RequestMapping("/")
@ConditionalOnProperty(prefix = "neuron", name = "standalone", havingValue = "true", matchIfMissing = false)

public class PortalController {

    private static final Logger log = LoggerFactory.getLogger(PortalController.class);

    private final NeuronConfig config;
    private final GameRegistryService gameRegistry;
    private final GameRouterService gameRouter;

    public PortalController(NeuronConfig config, GameRegistryService gameRegistry,
                            GameRouterService gameRouter) {
        this.config = config;
        this.gameRegistry = gameRegistry;
        this.gameRouter = gameRouter;
    }

    /**
     * Serve the player login/registration landing page.
     *
     * Returns a self-contained HTML page with login and registration forms.
     * After successful authentication, the page detects the player's platform,
     * fetches client distribution data, and redirects to the game dashboard
     * at /corporation/.
     */
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String portal() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>" + escapeHtml(config.getPortalTitle()) + "</title>\n" +
                "  <link rel=\"manifest\" href=\"/manifest.json\">\n" +
                "  <meta name=\"theme-color\" content=\"#0a0a1a\">\n" +
                "  <style>\n" +
                "    * { box-sizing: border-box; margin: 0; padding: 0; }\n" +
                "    body { font-family: 'Segoe UI', Arial, sans-serif; background: #0a0a1a; color: #e0e0e0; display: flex; justify-content: center; align-items: center; min-height: 100vh; }\n" +
                "    .container { max-width: 420px; width: 100%; padding: 40px; background: #131926; border-radius: 16px; box-shadow: 0 10px 40px rgba(0,0,0,0.5); }\n" +
                "    h1 { color: #00d4ff; font-size: 2em; text-align: center; margin-bottom: 6px; }\n" +
                "    .subtitle { color: #9aa0a6; text-align: center; margin-bottom: 30px; font-size: 0.95em; }\n" +
                "    .tabs { display: flex; margin-bottom: 24px; border-bottom: 2px solid #1e2840; }\n" +
                "    .tab { flex: 1; text-align: center; padding: 12px 0; cursor: pointer; color: #9aa0a6; font-size: 1em; transition: color 0.2s, border-color 0.2s; border-bottom: 2px solid transparent; margin-bottom: -2px; }\n" +
                "    .tab.active { color: #00d4ff; border-bottom-color: #00d4ff; }\n" +
                "    .tab:hover { color: #e0e0e0; }\n" +
                "    .form-group { margin-bottom: 18px; }\n" +
                "    .form-group label { display: block; margin-bottom: 6px; color: #9aa0a6; font-size: 0.85em; }\n" +
                "    .form-group input { width: 100%; padding: 12px 14px; background: #151d30; border: 1px solid rgba(255,255,255,0.1); border-radius: 8px; color: #e8eaed; font-size: 1em; outline: none; transition: border-color 0.2s; }\n" +
                "    .form-group input:focus { border-color: #00d4ff; }\n" +
                "    .form-group input::placeholder { color: #5f6368; }\n" +
                "    .btn { width: 100%; padding: 14px; background: #00d4ff; color: #0a0a1a; border: none; border-radius: 8px; font-size: 1em; font-weight: 600; cursor: pointer; transition: background 0.2s; margin-top: 8px; }\n" +
                "    .btn:hover { background: #00b8d4; }\n" +
                "    .btn:disabled { background: #3c4043; color: #5f6368; cursor: not-allowed; }\n" +
                "    .message { padding: 12px; border-radius: 8px; margin-bottom: 16px; font-size: 0.9em; text-align: center; display: none; }\n" +
                "    .message.error { display: block; background: rgba(239,83,80,0.12); color: #ef5350; border: 1px solid rgba(239,83,80,0.3); }\n" +
                "    .message.success { display: block; background: rgba(102,187,106,0.12); color: #66bb6a; border: 1px solid rgba(102,187,106,0.3); }\n" +
                "    .panel { display: none; }\n" +
                "    .panel.active { display: block; }\n" +
                "    .footer { text-align: center; margin-top: 24px; color: #5f6368; font-size: 0.8em; }\n" +
                "    .spinner { display: inline-block; width: 16px; height: 16px; border: 2px solid rgba(10,10,26,0.3); border-top-color: #0a0a1a; border-radius: 50%; animation: spin 0.6s linear infinite; margin-right: 6px; vertical-align: middle; }\n" +
                "    @keyframes spin { to { transform: rotate(360deg); } }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"container\">\n" +
                "    <h1>" + escapeHtml(config.getPortalTitle()) + "</h1>\n" +
                "    <p class=\"subtitle\">Player Gateway — Log in or create your account</p>\n" +
                "    <div class=\"tabs\">\n" +
                "      <div class=\"tab active\" id=\"tab-login\" onclick=\"switchTab('login')\">Log In</div>\n" +
                "      <div class=\"tab\" id=\"tab-register\" onclick=\"switchTab('register')\">Register</div>\n" +
                "    </div>\n" +
                "    <div id=\"message\" class=\"message\"></div>\n" +
                "\n" +
                "    <div id=\"panel-login\" class=\"panel active\">\n" +
                "      <form id=\"login-form\" onsubmit=\"handleLogin(event)\">\n" +
                "        <div class=\"form-group\">\n" +
                "          <label for=\"login-email\">Email</label>\n" +
                "          <input type=\"email\" id=\"login-email\" name=\"email\" placeholder=\"you@example.com\" required autocomplete=\"email\">\n" +
                "        </div>\n" +
                "        <div class=\"form-group\">\n" +
                "          <label for=\"login-password\">Password</label>\n" +
                "          <input type=\"password\" id=\"login-password\" name=\"password\" placeholder=\"Your password\" required autocomplete=\"current-password\">\n" +
                "        </div>\n" +
                "        <button type=\"submit\" class=\"btn\" id=\"login-btn\">Log In</button>\n" +
                "      </form>\n" +
                "    </div>\n" +
                "\n" +
                "    <div id=\"panel-register\" class=\"panel\">\n" +
                "      <form id=\"register-form\" onsubmit=\"handleRegister(event)\">\n" +
                "        <div class=\"form-group\">\n" +
                "          <label for=\"reg-email\">Email</label>\n" +
                "          <input type=\"email\" id=\"reg-email\" name=\"email\" placeholder=\"you@example.com\" required autocomplete=\"email\">\n" +
                "        </div>\n" +
                "        <div class=\"form-group\">\n" +
                "          <label for=\"reg-username\">Username</label>\n" +
                "          <input type=\"text\" id=\"reg-username\" name=\"username\" placeholder=\"Choose a username\" required autocomplete=\"username\">\n" +
                "        </div>\n" +
                "        <div class=\"form-group\">\n" +
                "          <label for=\"reg-password\">Password</label>\n" +
                "          <input type=\"password\" id=\"reg-password\" name=\"password\" placeholder=\"At least 6 characters\" required minlength=\"6\" autocomplete=\"new-password\">\n" +
                "        </div>\n" +
                "        <button type=\"submit\" class=\"btn\" id=\"reg-btn\">Create Account</button>\n" +
                "      </form>\n" +
                "    </div>\n" +
                "\n" +
                "    <p class=\"footer\">DoraMCU Player Gateway &copy; 2026</p>\n" +
                "  </div>\n" +
                "\n" +
                "  <script>\n" +
                "    function switchTab(tab) {\n" +
                "      document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));\n" +
                "      document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));\n" +
                "      document.getElementById('tab-' + tab).classList.add('active');\n" +
                "      document.getElementById('panel-' + tab).classList.add('active');\n" +
                "      hideMessage();\n" +
                "    }\n" +
                "\n" +
                "    function showMessage(text, type) {\n" +
                "      var el = document.getElementById('message');\n" +
                "      el.textContent = text;\n" +
                "      el.className = 'message ' + type;\n" +
                "    }\n" +
                "\n" +
                "    function hideMessage() {\n" +
                "      var el = document.getElementById('message');\n" +
                "      el.className = 'message';\n" +
                "      el.textContent = '';\n" +
                "    }\n" +
                "\n" +
                "    function setButtonLoading(btnId, loading) {\n" +
                "      var btn = document.getElementById(btnId);\n" +
                "      btn.disabled = loading;\n" +
                "      btn.innerHTML = loading ? '<span class=\"spinner\"></span> Please wait...' : btnId === 'login-btn' ? 'Log In' : 'Create Account';\n" +
                "    }\n" +
                "\n" +
                "    async function handleLogin(event) {\n" +
                "      event.preventDefault();\n" +
                "      hideMessage();\n" +
                "      var email = document.getElementById('login-email').value.trim();\n" +
                "      var password = document.getElementById('login-password').value;\n" +
                "      setButtonLoading('login-btn', true);\n" +
                "      try {\n" +
                "        var response = await fetch('/api/v1/player/login', {\n" +
                "          method: 'POST',\n" +
                "          headers: { 'Content-Type': 'application/json' },\n" +
                "          body: JSON.stringify({ email: email, password: password })\n" +
                "        });\n" +
                "        var data = await response.json();\n" +
                "        if (response.ok) {\n" +
                "          localStorage.setItem('accessToken', data.accessToken);\n" +
                "          localStorage.setItem('refreshToken', data.refreshToken || '');\n" +
                "          showMessage('Login successful! Redirecting...', 'success');\n" +
                "          detectPlatformAndRedirect();\n" +
                "        } else {\n" +
                "          showMessage(data.message || 'Login failed. Please check your credentials.', 'error');\n" +
                "        }\n" +
                "      } catch (e) {\n" +
                "        showMessage('Network error. Please try again.', 'error');\n" +
                "      } finally {\n" +
                "        setButtonLoading('login-btn', false);\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    async function handleRegister(event) {\n" +
                "      event.preventDefault();\n" +
                "      hideMessage();\n" +
                "      var email = document.getElementById('reg-email').value.trim();\n" +
                "      var username = document.getElementById('reg-username').value.trim();\n" +
                "      var password = document.getElementById('reg-password').value;\n" +
                "      setButtonLoading('reg-btn', true);\n" +
                "      try {\n" +
                "        var platform = detectPlatform();\n" +
                "        var response = await fetch('/api/v1/player/register', {\n" +
                "          method: 'POST',\n" +
                "          headers: { 'Content-Type': 'application/json' },\n" +
                "          body: JSON.stringify({ email: email, username: username, password: password, platform: platform.platform, os: platform.os })\n" +
                "        });\n" +
                "        var data = await response.json();\n" +
                "        if (response.ok) {\n" +
                "          localStorage.setItem('accessToken', data.accessToken);\n" +
                "          localStorage.setItem('refreshToken', data.refreshToken || '');\n" +
                "          showMessage('Account created! Redirecting...', 'success');\n" +
                "          detectPlatformAndRedirect();\n" +
                "        } else {\n" +
                "          showMessage(data.message || 'Registration failed. Please try again.', 'error');\n" +
                "        }\n" +
                "      } catch (e) {\n" +
                "        showMessage('Network error. Please try again.', 'error');\n" +
                "      } finally {\n" +
                "        setButtonLoading('reg-btn', false);\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    function detectPlatform() {\n" +
                "      var ua = navigator.userAgent;\n" +
                "      var os = 'unknown';\n" +
                "      if (/android/i.test(ua)) os = 'android';\n" +
                "      else if (/iPad|iPhone|iPod/.test(ua)) os = 'ios';\n" +
                "      else if (/win/i.test(ua)) os = 'windows';\n" +
                "      else if (/mac/i.test(ua)) os = 'macos';\n" +
                "      else if (/linux/i.test(ua)) os = 'linux';\n" +
                "      var platform = 'desktop';\n" +
                "      if (/Mobi|Android/i.test(ua)) platform = 'mobile';\n" +
                "      else if (/Tablet|iPad/i.test(ua)) platform = 'tablet';\n" +
                "      return { platform: platform, os: os };\n" +
                "    }\n" +
                "\n" +
                "    async function detectPlatformAndRedirect() {\n" +
                "      try {\n" +
                "        var platform = detectPlatform();\n" +
                "        var response = await fetch('/api/v1/player/platform?platform=' + encodeURIComponent(platform.platform) + '&os=' + encodeURIComponent(platform.os));\n" +
                "        if (response.ok) {\n" +
                "          var dist = await response.json();\n" +
                "          localStorage.setItem('clientDistribution', JSON.stringify(dist));\n" +
                "        }\n" +
                "      } catch (e) { /* ignore */ }\n" +
                "      window.location.href = '/corporation/';\n" +
                "    }\n" +
                "\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Serve the game dashboard after the player logs in.
     *
     * This is the post-login landing page where the player manages their
     * corporation, characters, and game sessions.  Replaces the old Angular
     * corporation dashboard with a self-contained HTML page.
     */
    @GetMapping(value = {"/corporation/", "/corporation/index.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public String corporationDashboard() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>" + escapeHtml(config.getPortalTitle()) + " — Dashboard</title>\n" +
                "  <style>\n" +
                "    * { box-sizing: border-box; margin: 0; padding: 0; }\n" +
                "    body { font-family: 'Segoe UI', Arial, sans-serif; background: #0a0a1a; color: #e0e0e0; min-height: 100vh; }\n" +
                "    .header { background: #131926; padding: 16px 24px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #1e2840; }\n" +
                "    .header h1 { color: #00d4ff; font-size: 1.4em; }\n" +
                "    .header .user-info { display: flex; align-items: center; gap: 12px; }\n" +
                "    .header .user-info span { color: #9aa0a6; font-size: 0.9em; }\n" +
                "    .logout-btn { background: #3c4043; color: #e0e0e0; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-size: 0.85em; }\n" +
                "    .logout-btn:hover { background: #5f6368; }\n" +
                "    .container { max-width: 960px; margin: 40px auto; padding: 0 24px; }\n" +
                "    .welcome { margin-bottom: 32px; }\n" +
                "    .welcome h2 { color: #e0e0e0; font-size: 1.6em; margin-bottom: 8px; }\n" +
                "    .welcome p { color: #9aa0a6; font-size: 0.95em; }\n" +
                "    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 20px; }\n" +
                "    .card { background: #131926; border-radius: 12px; padding: 24px; border: 1px solid #1e2840; transition: border-color 0.2s; }\n" +
                "    .card:hover { border-color: #00d4ff; }\n" +
                "    .card h3 { color: #00d4ff; font-size: 1.1em; margin-bottom: 8px; }\n" +
                "    .card p { color: #9aa0a6; font-size: 0.9em; line-height: 1.5; margin-bottom: 16px; }\n" +
                "    .card .action { display: inline-block; background: #00d4ff; color: #0a0a1a; padding: 8px 16px; border-radius: 6px; text-decoration: none; font-size: 0.85em; font-weight: 600; cursor: pointer; border: none; }\n" +
                "    .card .action:hover { background: #00b8d4; }\n" +
                "    .action { display: inline-block; background: #00d4ff; color: #0a0a1a; padding: 8px 16px; border-radius: 6px; text-decoration: none; font-size: 0.85em; font-weight: 600; cursor: pointer; border: none; }\n" +
                "    .action:hover { background: #00b8d4; }\n" +
                "    .status-bar { margin-top: 32px; padding: 16px 20px; background: #131926; border-radius: 10px; border: 1px solid #1e2840; }\n" +
                "    .status-bar h4 { color: #9aa0a6; font-size: 0.85em; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.5px; }\n" +
                "    .status-bar .status { display: flex; gap: 24px; flex-wrap: wrap; }\n" +
                "    .status-bar .status-item { display: flex; flex-direction: column; }\n" +
                "    .status-bar .status-item .label { color: #5f6368; font-size: 0.75em; text-transform: uppercase; }\n" +
                "    .status-bar .status-item .value { color: #e0e0e0; font-size: 1em; font-weight: 600; }\n" +
                "    .status-bar .status-item .value.online { color: #66bb6a; }\n" +
                "    .status-bar .status-item .value.offline { color: #ef5350; }\n" +
                "    .section-panel { display: none; margin-top: 24px; }\n" +
                "    .section-panel.active { display: block; }\n" +
                "    .section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 12px; }\n" +
                "    .section-header h3 { color: #00d4ff; font-size: 1.3em; }\n" +
                "    .section-filters { display: flex; gap: 8px; flex-wrap: wrap; }\n" +
                "    .section-filters select { background: #151d30; color: #e0e0e0; border: 1px solid #1e2840; padding: 6px 10px; border-radius: 6px; font-size: 0.85em; }\n" +
                "    .section-filters select:focus { border-color: #00d4ff; outline: none; }\n" +
                "    .back-btn { background: #3c4043; color: #e0e0e0; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-size: 0.85em; }\n" +
                "    .back-btn:hover { background: #5f6368; }\n" +
                "    .item-list { display: grid; gap: 12px; }\n" +
                "    .item-card { background: #131926; border-radius: 10px; padding: 16px 20px; border: 1px solid #1e2840; transition: border-color 0.2s; }\n" +
                "    .item-card:hover { border-color: #00d4ff; }\n" +
                "    .item-card .item-title { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px; }\n" +
                "    .item-card .item-title h4 { color: #e0e0e0; font-size: 1.05em; }\n" +
                "    .item-card .item-title .item-source { color: #5f6368; font-size: 0.75em; background: #151d30; padding: 2px 8px; border-radius: 4px; text-transform: uppercase; }\n" +
                "    .item-card .item-desc { color: #9aa0a6; font-size: 0.85em; line-height: 1.5; margin-bottom: 12px; }\n" +
                "    .item-card .item-meta { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 12px; }\n" +
                "    .item-card .item-meta .meta-item { display: flex; flex-direction: column; }\n" +
                "    .item-card .item-meta .meta-item .meta-label { color: #5f6368; font-size: 0.7em; text-transform: uppercase; }\n" +
                "    .item-card .item-meta .meta-item .meta-value { color: #e0e0e0; font-size: 0.9em; font-weight: 600; }\n" +
                "    .item-card .item-actions { display: flex; gap: 8px; }\n" +
                "    .item-card .item-actions .primary-btn { background: #00d4ff; color: #0a0a1a; border: none; padding: 6px 14px; border-radius: 6px; cursor: pointer; font-size: 0.8em; font-weight: 600; }\n" +
                "    .item-card .item-actions .primary-btn:hover { background: #00b8d4; }\n" +
                "    .item-card .item-actions .secondary-btn { background: #3c4043; color: #e0e0e0; border: none; padding: 6px 14px; border-radius: 6px; cursor: pointer; font-size: 0.8em; }\n" +
                "    .item-card .item-actions .secondary-btn:hover { background: #5f6368; }\n" +
                "    .loading-msg { text-align: center; padding: 40px; color: #9aa0a6; }\n" +
                "    .empty-msg { text-align: center; padding: 40px; color: #5f6368; }\n" +
                "    .detail-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(120px, 1fr)); gap: 8px; margin-bottom: 12px; }\n" +
                "    .detail-grid .detail-item { background: #151d30; padding: 8px 12px; border-radius: 6px; }\n" +
                "    .detail-grid .detail-item .detail-label { color: #5f6368; font-size: 0.7em; text-transform: uppercase; }\n" +
                "    .detail-grid .detail-item .detail-value { color: #e0e0e0; font-size: 0.95em; font-weight: 600; }\n" +
                "    .corp-overview { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 12px; margin-bottom: 20px; }\n" +
                "    .corp-stat { background: #151d30; padding: 16px; border-radius: 8px; text-align: center; }\n" +
                "    .corp-stat .stat-value { color: #00d4ff; font-size: 1.8em; font-weight: 700; }\n" +
                "    .corp-stat .stat-label { color: #5f6368; font-size: 0.75em; text-transform: uppercase; margin-top: 4px; }\n" +
                "    .galaxy-info { background: #151d30; padding: 16px; border-radius: 8px; margin-bottom: 16px; }\n" +
                "    .galaxy-info h4 { color: #00d4ff; margin-bottom: 8px; }\n" +
                "    .galaxy-info p { color: #9aa0a6; font-size: 0.9em; line-height: 1.5; }\n" +
                "    .combat-type-card { background: #151d30; padding: 16px; border-radius: 8px; cursor: pointer; border: 1px solid #1e2840; transition: border-color 0.2s; }\n" +
                "    .combat-type-card:hover { border-color: #00d4ff; }\n" +
                "    .combat-type-card h4 { color: #00d4ff; margin-bottom: 4px; }\n" +
                "    .combat-type-card p { color: #9aa0a6; font-size: 0.85em; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"header\">\n" +
                "    <h1>" + escapeHtml(config.getPortalTitle()) + "</h1>\n" +
                "    <div class=\"user-info\">\n" +
                "      <span id=\"player-name\">Loading...</span>\n" +
                "      <button class=\"logout-btn\" onclick=\"handleLogout()\">Log Out</button>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "  <div class=\"container\">\n" +
                "    <div id=\"main-view\">\n" +
                "      <div class=\"welcome\">\n" +
                "        <h2>Welcome back, Commander</h2>\n" +
                "        <p>Your local game node is ready. Manage your corporation, characters, and missions below.</p>\n" +
                "      </div>\n" +
                "      <div class=\"grid\">\n" +
                "        <div class=\"card\">\n" +
                "          <h3>🏢 Corporation</h3>\n" +
                "          <p>Manage your corporation's resources, assets, and operations across the galaxy.</p>\n" +
                "          <button class=\"action\" onclick=\"loadCorporation()\">Open Corporation</button>\n" +
                "        </div>\n" +
                "        <div class=\"card\">\n" +
                "          <h3>👤 Characters</h3>\n" +
                "          <p>View and manage your characters, their skills, equipment, and progression.</p>\n" +
                "          <button class=\"action\" onclick=\"loadCharacters()\">View Characters</button>\n" +
                "        </div>\n" +
                "        <div class=\"card\">\n" +
                "          <h3>🚀 Missions</h3>\n" +
                "          <p>Browse available missions, accept contracts, and track active operations.</p>\n" +
                "          <button class=\"action\" onclick=\"loadMissions()\">Browse Missions</button>\n" +
                "        </div>\n" +
                "        <div class=\"card\">\n" +
                "          <h3>🌌 Galaxy Map</h3>\n" +
                "          <p>Explore star systems, navigate sectors, and discover new opportunities.</p>\n" +
                "          <button class=\"action\" onclick=\"loadGalaxy()\">Explore Galaxy</button>\n" +
                "        </div>\n" +
                "        <div class=\"card\">\n" +
                "          <h3>⚔️ Combat</h3>\n" +
                "          <p>Enter combat simulations, train your characters, and test your strategies.</p>\n" +
                "          <button class=\"action\" onclick=\"loadCombat()\">Start Combat</button>\n" +
                "        </div>\n" +
                "        <div class=\"card\">\n" +
                 "          <h3>🌍 Earth 2026</h3>\n" +
                 "          <p>Land on Earth in 2026, create your character, and begin your adventure in San Diego.</p>\n" +
                 "          <button class=\"action\" onclick=\"loadEarth2026()\">Land on Earth</button>\n" +
                 "        </div>\n" +
                 "        <div class=\"card\">\n" +
                 "          <h3>📡 Game Node Status</h3>\n" +
                 "          <p>Check your local game engine status, sync progress, and AI model health.</p>\n" +
                 "          <button class=\"action\" onclick=\"loadStatus()\">Check Status</button>\n" +
                 "        </div>\n" +
                 "      </div>\n" +
                "      <div class=\"status-bar\">\n" +
                "        <h4>Game Node Status</h4>\n" +
                "        <div class=\"status\">\n" +
                "          <div class=\"status-item\">\n" +
                "            <span class=\"label\">Connection</span>\n" +
                "            <span class=\"value online\" id=\"conn-status\">● Connected</span>\n" +
                "          </div>\n" +
                "          <div class=\"status-item\">\n" +
                "            <span class=\"label\">Game Engine</span>\n" +
                "            <span class=\"value\" id=\"engine-status\">Loading...</span>\n" +
                "          </div>\n" +
                "          <div class=\"status-item\">\n" +
                "            <span class=\"label\">AI Model</span>\n" +
                "            <span class=\"value\" id=\"ai-status\">Loading...</span>\n" +
                "          </div>\n" +
                "          <div class=\"status-item\">\n" +
                "            <span class=\"label\">Last Sync</span>\n" +
                "            <span class=\"value\" id=\"sync-status\">Never</span>\n" +
                "          </div>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "    <div id=\"missions-panel\" class=\"section-panel\">\n" +
                "      <div class=\"section-header\">\n" +
                "        <div style=\"display:flex;align-items:center;gap:12px;\">\n" +
                "          <button class=\"back-btn\" onclick=\"backToMain()\">← Back</button>\n" +
                "          <h3>🚀 Available Missions</h3>\n" +
                "        </div>\n" +
                "        <div class=\"section-filters\">\n" +
                "          <select id=\"mission-filter-type\" onchange=\"filterSectionItems('mission')\">\n" +
                "            <option value=\"\">All Types</option>\n" +
                "            <option value=\"ASSAULT\">Assault</option>\n" +
                "            <option value=\"DEFENSE\">Defense</option>\n" +
                "            <option value=\"ESCORT\">Escort</option>\n" +
                "            <option value=\"RECON\">Recon</option>\n" +
                "            <option value=\"SABOTAGE\">Sabotage</option>\n" +
                "          </select>\n" +
                "          <select id=\"mission-filter-difficulty\" onchange=\"filterSectionItems('mission')\">\n" +
                "            <option value=\"\">All Difficulties</option>\n" +
                "            <option value=\"EASY\">Easy</option>\n" +
                "            <option value=\"NORMAL\">Normal</option>\n" +
                "            <option value=\"HARD\">Hard</option>\n" +
                "            <option value=\"EXTREME\">Extreme</option>\n" +
                "          </select>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "      <div id=\"mission-loading\" class=\"loading-msg\">Loading missions...</div>\n" +
                "      <div id=\"mission-list\" class=\"item-list\"></div>\n" +
                "      <div id=\"mission-empty\" class=\"empty-msg\" style=\"display:none;\">No missions available. Check back later, Commander.</div>\n" +
                "    </div>\n" +
                "    <div id=\"characters-panel\" class=\"section-panel\">\n" +
                "      <div class=\"section-header\">\n" +
                "        <div style=\"display:flex;align-items:center;gap:12px;\">\n" +
                "          <button class=\"back-btn\" onclick=\"backToMain()\">← Back</button>\n" +
                "          <h3>👤 Your Characters</h3>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "      <div id=\"character-loading\" class=\"loading-msg\">Loading characters...</div>\n" +
                "      <div id=\"character-list\" class=\"item-list\"></div>\n" +
                "      <div id=\"character-empty\" class=\"empty-msg\" style=\"display:none;\">No characters found. Create one to get started!</div>\n" +
                "    </div>\n" +
                "    <div id=\"corporation-panel\" class=\"section-panel\">\n" +
                "      <div class=\"section-header\">\n" +
                "        <div style=\"display:flex;align-items:center;gap:12px;\">\n" +
                "          <button class=\"back-btn\" onclick=\"backToMain()\">← Back</button>\n" +
                "          <h3>🏢 Corporation Overview</h3>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "      <div id=\"corp-loading\" class=\"loading-msg\">Loading corporation data...</div>\n" +
                "      <div id=\"corp-content\" style=\"display:none;\">\n" +
                "        <div class=\"corp-overview\" id=\"corp-stats\"></div>\n" +
                "        <h4 style=\"color:#00d4ff;margin-bottom:12px;\">Active Contracts</h4>\n" +
                "        <div id=\"corp-contracts\" class=\"item-list\"></div>\n" +
                "        <div id=\"corp-contracts-empty\" class=\"empty-msg\" style=\"display:none;\">No active contracts.</div>\n" +
                "      </div>\n" +
                "      <div id=\"corp-empty\" class=\"empty-msg\" style=\"display:none;\">No corporation found. Complete onboarding to create one!</div>\n" +
                "    </div>\n" +
                "    <div id=\"galaxy-panel\" class=\"section-panel\">\n" +
                "      <div class=\"section-header\">\n" +
                "        <div style=\"display:flex;align-items:center;gap:12px;\">\n" +
                "          <button class=\"back-btn\" onclick=\"backToMain()\">← Back</button>\n" +
                "          <h3>🌌 Galaxy Map</h3>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "      <div id=\"galaxy-loading\" class=\"loading-msg\">Loading galaxy data...</div>\n" +
                "      <div id=\"galaxy-content\" style=\"display:none;\">\n" +
                "        <div class=\"galaxy-info\" id=\"galaxy-info\"></div>\n" +
                "        <div id=\"galaxy-sectors\" class=\"item-list\"></div>\n" +
                "      </div>\n" +
                "      <div id=\"galaxy-empty\" class=\"empty-msg\" style=\"display:none;\">Galaxy data not available yet.</div>\n" +
                "    </div>\n" +
                "    <div id=\"combat-panel\" class=\"section-panel\">\n" +
                "      <div class=\"section-header\">\n" +
                "        <div style=\"display:flex;align-items:center;gap:12px;\">\n" +
                "          <button class=\"back-btn\" onclick=\"backToMain()\">← Back</button>\n" +
                "          <h3>⚔️ Combat</h3>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "      <div id=\"combat-loading\" class=\"loading-msg\">Loading combat types...</div>\n" +
                "      <div id=\"combat-types\" class=\"item-list\" style=\"display:none;\"></div>\n" +
                "      <div id=\"combat-active\" style=\"display:none;\">\n" +
                "        <h4 style=\"color:#00d4ff;margin:16px 0 12px;\">Active Battles</h4>\n" +
                "        <div id=\"combat-active-list\" class=\"item-list\"></div>\n" +
                "        <div id=\"combat-active-empty\" class=\"empty-msg\" style=\"display:none;\">No active battles.</div>\n" +
                "      </div>\n" +
                "      <div id=\"combat-empty\" class=\"empty-msg\" style=\"display:none;\">No combat systems available.</div>\n" +
                "    </div>\n" +
                "    <div id=\"status-panel\" class=\"section-panel\">\n" +
                 "      <div class=\"section-header\">\n" +
                 "        <div style=\"display:flex;align-items:center;gap:12px;\">\n" +
                 "          <button class=\"back-btn\" onclick=\"backToMain()\">← Back</button>\n" +
                 "          <h3>📡 Game Node Status</h3>\n" +
                 "        </div>\n" +
                 "      </div>\n" +
                 "      <div id=\"status-content\">\n" +
                 "        <div class=\"detail-grid\" id=\"status-details\"></div>\n" +
                 "      </div>\n" +
                 "    </div>\n" +
                 "    <div id=\"earth2026-panel\" class=\"section-panel\">\n" +
                 "      <div class=\"section-header\">\n" +
                 "        <div style=\"display:flex;align-items:center;gap:12px;\">\n" +
                 "          <button class=\"back-btn\" onclick=\"backToMain()\">← Back</button>\n" +
                 "          <h3>🌍 Earth 2026</h3>\n" +
                 "        </div>\n" +
                 "      </div>\n" +
                 "      <div id=\"earth2026-loading\" class=\"loading-msg\">Loading Earth 2026 data...</div>\n" +
                 "      <div id=\"earth2026-content\" style=\"display:none;\">\n" +
                 "        <div class=\"galaxy-info\" id=\"earth2026-info\"></div>\n" +
                 "        <div id=\"sol-system-panel\" style=\"background:#151d30;padding:16px;border-radius:8px;margin-bottom:16px;display:none;\">\n" +
                 "          <h4 style=\"color:#00d4ff;margin-bottom:12px;\">☀️ Sol System - Earth's Solar System</h4>\n" +
                 "          <div id=\"sol-system-worlds\" class=\"item-list\"></div>\n" +
                 "        </div>\n" +
                 "        <div id=\"earth2026-character-form\" style=\"background:#151d30;padding:16px;border-radius:8px;margin-bottom:16px;\">\n" +
                 "          <h4 style=\"color:#00d4ff;margin-bottom:12px;\">Create Your Character</h4>\n" +
                 "          <div style=\"display:flex;flex-direction:column;gap:12px;\">\n" +
                 "            <div>\n" +
                 "              <label style=\"color:#9aa0a6;font-size:0.85em;display:block;margin-bottom:4px;\">Character Name</label>\n" +
                 "              <input type=\"text\" id=\"e26-char-name\" placeholder=\"Enter your character name\" style=\"width:100%;padding:10px 12px;background:#0a0a1a;border:1px solid #1e2840;border-radius:6px;color:#e0e0e0;font-size:0.95em;\">\n" +
                 "            </div>\n" +
                 "            <div>\n" +
                 "              <label style=\"color:#9aa0a6;font-size:0.85em;display:block;margin-bottom:4px;\">Age</label>\n" +
                 "              <input type=\"number\" id=\"e26-char-age\" value=\"25\" min=\"16\" max=\"100\" style=\"width:100%;padding:10px 12px;background:#0a0a1a;border:1px solid #1e2840;border-radius:6px;color:#e0e0e0;font-size:0.95em;\">\n" +
                 "            </div>\n" +
                 "            <div>\n" +
                 "              <label style=\"color:#9aa0a6;font-size:0.85em;display:block;margin-bottom:4px;\">Occupation (optional)</label>\n" +
                 "              <input type=\"text\" id=\"e26-char-occupation\" placeholder=\"e.g. Engineer, Doctor, Soldier\" style=\"width:100%;padding:10px 12px;background:#0a0a1a;border:1px solid #1e2840;border-radius:6px;color:#e0e0e0;font-size:0.95em;\">\n" +
                 "            </div>\n" +
                 "            <button class=\"action\" onclick=\"createEarthCharacter()\" style=\"margin-top:8px;\">Create Character &amp; Land on Earth</button>\n" +
                 "          </div>\n" +
                 "          <div id=\"e26-char-result\" style=\"margin-top:12px;\"></div>\n" +
                 "        </div>\n" +
                 "        <h4 style=\"color:#00d4ff;margin-bottom:12px;\">Available Locations</h4>\n" +
                 "        <div id=\"earth2026-locations\" class=\"item-list\"></div>\n" +
                 "        <div id=\"earth2026-locations-empty\" class=\"empty-msg\" style=\"display:none;\">No locations available yet.</div>\n" +
                 "        <h4 style=\"color:#00d4ff;margin:16px 0 12px;\">Starting Scenarios</h4>\n" +
                 "        <div id=\"earth2026-scenarios\" class=\"item-list\"></div>\n" +
                 "        <div id=\"earth2026-scenarios-empty\" class=\"empty-msg\" style=\"display:none;\">No scenarios available yet.</div>\n" +
                 "      </div>\n" +
                 "      <div id=\"earth2026-empty\" class=\"empty-msg\" style=\"display:none;\">Earth 2026 data not available yet.</div>\n" +
                 "    </div>\n" +
                 "  </div>\n" +
                "  <script>\n" +
                "    var allMissions = [];\n" +
                 "    var allCharacters = [];\n" +
                 "    var allCombatTypes = [];\n" +
                 "    var allActiveCombat = [];\n" +
                 "    var allEarth2026Locations = [];\n" +
                 "    var allEarth2026Scenarios = [];\n" +
                 "    var earth2026Username = '';\n" +
                "\n" +
                "    function handleLogout() {\n" +
                "      localStorage.removeItem('accessToken');\n" +
                "      localStorage.removeItem('refreshToken');\n" +
                "      localStorage.removeItem('clientDistribution');\n" +
                "      window.location.href = '/';\n" +
                "    }\n" +
                "\n" +
                "    function backToMain() {\n" +
                "      document.querySelectorAll('.section-panel').forEach(function(p) { p.classList.remove('active'); });\n" +
                "      document.getElementById('main-view').style.display = 'block';\n" +
                "    }\n" +
                "\n" +
                "    function showPanel(panelId) {\n" +
                "      document.getElementById('main-view').style.display = 'none';\n" +
                "      document.querySelectorAll('.section-panel').forEach(function(p) { p.classList.remove('active'); });\n" +
                "      document.getElementById(panelId).classList.add('active');\n" +
                "    }\n" +
                "\n" +
                "    function escapeText(text) {\n" +
                "      var div = document.createElement('div');\n" +
                "      div.textContent = text || '';\n" +
                "      return div.innerHTML;\n" +
                "    }\n" +
                "\n" +
                "    /* ===== MISSIONS ===== */\n" +
                "\n" +
                "    function loadMissions() {\n" +
                "      showPanel('missions-panel');\n" +
                "      document.getElementById('mission-loading').style.display = 'block';\n" +
                "      document.getElementById('mission-list').innerHTML = '';\n" +
                "      document.getElementById('mission-empty').style.display = 'none';\n" +
                "      var token = localStorage.getItem('accessToken');\n" +
                "      console.log('[Dashboard] Loading missions, token:', token ? 'present' : 'missing');\n" +
                "      var headers = {};\n" +
                "      if (token) headers['Authorization'] = 'Bearer ' + token;\n" +
                "      fetch('/api/v1/missions', { headers: headers })\n" +
                "        .then(function(r) {\n" +
                "          if (!r.ok) throw new Error('HTTP ' + r.status + ' ' + r.statusText);\n" +
                "          return r.json();\n" +
                "        })\n" +
                "        .then(function(data) {\n" +
                "          allMissions = data || [];\n" +
                "          console.log('[Dashboard] Missions loaded:', allMissions.length);\n" +
                "          document.getElementById('mission-loading').style.display = 'none';\n" +
                "          renderSectionItems('mission', allMissions, ['name','type','difficulty','planet','faction','creditsReward','experienceReward']);\n" +
                "        })\n" +
                "        .catch(function(e) {\n" +
                "          console.error('[Dashboard] Failed to load missions:', e);\n" +
                "          document.getElementById('mission-loading').style.display = 'none';\n" +
                "          document.getElementById('mission-empty').style.display = 'block';\n" +
                "        });\n" +
                "    }\n" +
                "\n" +
                "    function filterSectionItems(section) {\n" +
                "      var typeFilter = document.getElementById(section + '-filter-type').value;\n" +
                "      var diffFilter = document.getElementById(section + '-filter-difficulty').value;\n" +
                "      var items = section === 'mission' ? allMissions : allCharacters;\n" +
                "      var filtered = items;\n" +
                "      if (typeFilter) filtered = filtered.filter(function(m) { return m.type === typeFilter; });\n" +
                "      if (diffFilter) filtered = filtered.filter(function(m) { return m.difficulty === diffFilter; });\n" +
                "      renderSectionItems(section, filtered, ['name','type','difficulty','planet','faction','creditsReward','experienceReward']);\n" +
                "    }\n" +
                "\n" +
                "    /* ===== CHARACTERS ===== */\n" +
                "\n" +
                "    function loadCharacters() {\n" +
                "      showPanel('characters-panel');\n" +
                "      document.getElementById('character-loading').style.display = 'block';\n" +
                "      document.getElementById('character-list').innerHTML = '';\n" +
                "      document.getElementById('character-empty').style.display = 'none';\n" +
                "      var token = localStorage.getItem('accessToken');\n" +
                "      console.log('[Dashboard] Loading characters, token:', token ? 'present' : 'missing');\n" +
                "      var headers = {};\n" +
                "      if (token) headers['Authorization'] = 'Bearer ' + token;\n" +
                "      fetch('/api/v1/characters', { headers: headers })\n" +
                "        .then(function(r) {\n" +
                "          if (!r.ok) throw new Error('HTTP ' + r.status + ' ' + r.statusText);\n" +
                "          return r.json();\n" +
                "        })\n" +
                "        .then(function(data) {\n" +
                "          allCharacters = data || [];\n" +
                "          console.log('[Dashboard] Characters loaded:', allCharacters.length);\n" +
                "          document.getElementById('character-loading').style.display = 'none';\n" +
                "          renderCharacters(allCharacters);\n" +
                "        })\n" +
                "        .catch(function(e) {\n" +
                "          console.error('[Dashboard] Failed to load characters:', e);\n" +
                "          document.getElementById('character-loading').style.display = 'none';\n" +
                "          document.getElementById('character-empty').style.display = 'block';\n" +
                "        });\n" +
                "    }\n" +
                "\n" +
                "    function renderCharacters(chars) {\n" +
                "      var list = document.getElementById('character-list');\n" +
                "      var empty = document.getElementById('character-empty');\n" +
                "      if (!chars || chars.length === 0) {\n" +
                "        list.innerHTML = '';\n" +
                "        empty.style.display = 'block';\n" +
                "        return;\n" +
                "      }\n" +
                "      empty.style.display = 'none';\n" +
                "      var html = '';\n" +
                "      chars.forEach(function(c) {\n" +
                "        html += '<div class=\"item-card\">';\n" +
                "        html += '  <div class=\"item-title\">';\n" +
                "        html += '    <h4>' + escapeText(c.name) + '</h4>';\n" +
                "        html += '    <span class=\"item-source\">' + escapeText(c.source || '') + '</span>';\n" +
                "        html += '  </div>';\n" +
                "        if (c.class) html += '  <div class=\"item-desc\">Class: ' + escapeText(c.class) + '</div>';\n" +
                "        html += '  <div class=\"item-meta\">';\n" +
                "        if (c.level) html += '    <div class=\"meta-item\"><span class=\"meta-label\">Level</span><span class=\"meta-value\">' + c.level + '</span></div>';\n" +
                "        if (c.hitPoints) html += '    <div class=\"meta-item\"><span class=\"meta-label\">HP</span><span class=\"meta-value\">' + c.hitPoints + '</span></div>';\n" +
                "        if (c.strength) html += '    <div class=\"meta-item\"><span class=\"meta-label\">STR</span><span class=\"meta-value\">' + c.strength + '</span></div>';\n" +
                "        if (c.dexterity) html += '    <div class=\"meta-item\"><span class=\"meta-label\">DEX</span><span class=\"meta-value\">' + c.dexterity + '</span></div>';\n" +
                "        if (c.intelligence) html += '    <div class=\"meta-item\"><span class=\"meta-label\">INT</span><span class=\"meta-value\">' + c.intelligence + '</span></div>';\n" +
                "        if (c.credits) html += '    <div class=\"meta-item\"><span class=\"meta-label\">Credits</span><span class=\"meta-value\">' + c.credits + '</span></div>';\n" +
                "        html += '  </div>';\n" +
                "        html += '  <div class=\"item-actions\">';\n" +
                "        html += '    <button class=\"action\" data-char-id=\"' + escapeText(String(c.id || c.name || '')) + '\" data-char-source=\"' + escapeText(String(c.source || '')) + '\" onclick=\"launchCharacter(this.dataset.charId, this.dataset.charSource)\">🚀 Launch</button>';\n" +
                "        html += '    <button class=\"secondary-btn\" onclick=\"alert(\\'Character details for: \\' + \\'\\'' + (c.name||'') + '\\'\\')\\'>Details</button>';\n" +
                "        html += '  </div>';\n" +
                "        html += '</div>';\n" +
                "      });\n" +
                "      list.innerHTML = html;\n" +
                "    }\n" +
                "\n" +
                "    function launchCharacter(characterId, source) {\n" +
                "      console.log('[Dashboard] Launching character:', characterId, 'source:', source);\n" +
                "      var token = localStorage.getItem('accessToken');\n" +
                "      // Store the selected character info for the game session\n" +
                "      localStorage.setItem('selectedCharacterId', String(characterId));\n" +
                "      localStorage.setItem('selectedCharacterSource', String(source || ''));\n" +
                "      // Determine the game URL based on the character's source\n" +
                "      var gameUrl;\n" +
                "      if (source) {\n" +
                "        var s = source.toLowerCase();\n" +
                "        if (s.indexOf('earth') !== -1 || s.indexOf('earth2026') !== -1) {\n" +
                "          gameUrl = '/earth2026/';\n" +
                "        } else if (s.indexOf('traveller') !== -1) {\n" +
                "          gameUrl = '/game/traveller';\n" +
                "        } else if (s.indexOf('mech') !== -1 || s.indexOf('mechwarrior') !== -1) {\n" +
                "          gameUrl = '/game/mechwarrior';\n" +
                "        } else if (s.indexOf('captain') !== -1 || s.indexOf('captainslog') !== -1) {\n" +
                "          gameUrl = '/game/captainslog';\n" +
                "        } else {\n" +
                "          gameUrl = '/game/';\n" +
                "        }\n" +
                "      } else {\n" +
                "        gameUrl = '/game/';\n" +
                "      }\n" +
                "      // Redirect to the game with character info\n" +
                "      window.location.href = gameUrl + '?characterId=' + encodeURIComponent(characterId);\n" +
                "    }\n" +
                "\n" +
                "    /* ===== CORPORATION ===== */\n" +
                "\n" +
                "    function loadCorporation() {\n" +
                "      showPanel('corporation-panel');\n" +
                "      document.getElementById('corp-loading').style.display = 'block';\n" +
                "      document.getElementById('corp-content').style.display = 'none';\n" +
                "      document.getElementById('corp-empty').style.display = 'none';\n" +
                "      var token = localStorage.getItem('accessToken');\n" +
                "      console.log('[Dashboard] Loading corporation, token:', token ? 'present' : 'missing');\n" +
                "      var headers = {};\n" +
                "      if (token) headers['Authorization'] = 'Bearer ' + token;\n" +
                "      // Try to get corporation data; use corp ID 1 as default\n" +
                "      fetch('/api/v1/corporate/office/1', { headers: headers })\n" +
                "        .then(function(r) {\n" +
                "          if (!r.ok) throw new Error('HTTP ' + r.status + ' ' + r.statusText);\n" +
                "          return r.json();\n" +
                "        })\n" +
                "        .then(function(data) {\n" +
                "          console.log('[Dashboard] Corporation loaded:', data ? data.corporationName || 'yes' : 'null');\n" +
                "          document.getElementById('corp-loading').style.display = 'none';\n" +
                "          if (!data) {\n" +
                "            document.getElementById('corp-empty').style.display = 'block';\n" +
                "            return;\n" +
                "          }\n" +
                "          document.getElementById('corp-content').style.display = 'block';\n" +
                "          var statsHtml = '';\n" +
                "          if (data.creditBalance) statsHtml += '<div class=\"corp-stat\"><div class=\"stat-value\">' + data.creditBalance + '</div><div class=\"stat-label\">Credits</div></div>';\n" +
                "          if (data.corporationName) statsHtml += '<div class=\"corp-stat\"><div class=\"stat-value\">' + escapeText(data.corporationName) + '</div><div class=\"stat-label\">Name</div></div>';\n" +
                "          if (data.officeBuildingLevel !== undefined) statsHtml += '<div class=\"corp-stat\"><div class=\"stat-value\">' + data.officeBuildingLevel + '</div><div class=\"stat-label\">Building Level</div></div>';\n" +
                "          if (data.activeHireCount) statsHtml += '<div class=\"corp-stat\"><div class=\"stat-value\">' + data.activeHireCount + '</div><div class=\"stat-label\">Staff</div></div>';\n" +
                "          if (data.currentRole) statsHtml += '<div class=\"corp-stat\"><div class=\"stat-value\">' + escapeText(data.currentRole) + '</div><div class=\"stat-label\">Your Role</div></div>';\n" +
                "          if (!statsHtml) statsHtml = '<div class=\"corp-stat\"><div class=\"stat-value\">—</div><div class=\"stat-label\">No data</div></div>';\n" +
                "          document.getElementById('corp-stats').innerHTML = statsHtml;\n" +
                "          // Render available contracts\n" +
                "          var contracts = data.availableContracts || [];\n" +
                "          var contractsList = document.getElementById('corp-contracts');\n" +
                "          var contractsEmpty = document.getElementById('corp-contracts-empty');\n" +
                "          if (contracts.length > 0) {\n" +
                "            contractsEmpty.style.display = 'none';\n" +
                "            var cHtml = '';\n" +
                "            contracts.forEach(function(ct) {\n" +
                "              cHtml += '<div class=\"item-card\"><div class=\"item-title\"><h4>' + escapeText(ct.title || ct.name) + '</h4></div>';\n" +
                "              if (ct.type) cHtml += '<div class=\"item-desc\">Type: ' + escapeText(ct.type) + '</div>';\n" +
                "              if (ct.payment) cHtml += '<div class=\"item-desc\">Payment: ' + ct.payment + ' credits</div>';\n" +
                "              cHtml += '</div>';\n" +
                "            });\n" +
                "            contractsList.innerHTML = cHtml;\n" +
                "          } else {\n" +
                "            contractsEmpty.style.display = 'block';\n" +
                "            contractsList.innerHTML = '';\n" +
                "          }\n" +
                "        })\n" +
                "        .catch(function(e) {\n" +
                "          console.error('[Dashboard] Failed to load corporation:', e);\n" +
                "          document.getElementById('corp-loading').style.display = 'none';\n" +
                "          document.getElementById('corp-empty').style.display = 'block';\n" +
                "        });\n" +
                "    }\n" +
                "\n" +
                "    /* ===== GALAXY ===== */\n" +
                "\n" +
                "    function loadGalaxy() {\n" +
                "      showPanel('galaxy-panel');\n" +
                "      document.getElementById('galaxy-loading').style.display = 'block';\n" +
                "      document.getElementById('galaxy-content').style.display = 'none';\n" +
                "      document.getElementById('galaxy-empty').style.display = 'none';\n" +
                "      var token = localStorage.getItem('accessToken');\n" +
                "      console.log('[Dashboard] Loading galaxy, token:', token ? 'present' : 'missing');\n" +
                "      var headers = {};\n" +
                "      if (token) headers['Authorization'] = 'Bearer ' + token;\n" +
                "      // Load galaxy statistics and sectors\n" +
                "      Promise.all([\n" +
                "        fetch('/api/v1/galaxy-3d/statistics', { headers: headers }).then(function(r) {\n" +
                "          if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
                "          return r.json();\n" +
                "        }).catch(function(e) { console.warn('[Dashboard] Galaxy stats failed:', e); return null; }),\n" +
                "        fetch('/api/v1/4x/sectors', { headers: headers }).then(function(r) {\n" +
                "          if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
                "          return r.json();\n" +
                "        }).catch(function(e) { console.warn('[Dashboard] Sectors failed:', e); return []; })\n" +
                "      ]).then(function(results) {\n" +
                "        var stats = results[0];\n" +
                "        var sectors = results[1];\n" +
                "        console.log('[Dashboard] Galaxy loaded - stats:', stats ? 'yes' : 'no', 'sectors:', sectors ? sectors.length : 0);\n" +
                "        document.getElementById('galaxy-loading').style.display = 'none';\n" +
                "        if (!stats && (!sectors || sectors.length === 0)) {\n" +
                "          document.getElementById('galaxy-empty').style.display = 'block';\n" +
                "          return;\n" +
                "        }\n" +
                "        document.getElementById('galaxy-content').style.display = 'block';\n" +
                "        // Galaxy info\n" +
                "        var infoHtml = '<h4>Galaxy Overview</h4>';\n" +
                "        if (stats) {\n" +
                "          if (stats.totalBodies) infoHtml += '<p>Total Bodies: ' + stats.totalBodies + '</p>';\n" +
                "          if (stats.stars) infoHtml += '<p>Stars: ' + stats.stars + '</p>';\n" +
                "          if (stats.planets) infoHtml += '<p>Planets: ' + stats.planets + '</p>';\n" +
                "          if (stats.bodyTypes) infoHtml += '<p>Body Types: ' + stats.bodyTypes + '</p>';\n" +
                "          if (stats.colorSchemes) infoHtml += '<p>Color Schemes: ' + stats.colorSchemes + '</p>';\n" +
                "          if (stats.cameraPresets) infoHtml += '<p>Camera Presets: ' + stats.cameraPresets + '</p>';\n" +
                "        }\n" +
                "        document.getElementById('galaxy-info').innerHTML = infoHtml;\n" +
                "        // Sectors - sorted with Sol first\n" +
                "        var sectorsList = document.getElementById('galaxy-sectors');\n" +
                "        if (sectors && sectors.length > 0) {\n" +
                "          // Sort sectors: Sol first, then alphabetically\n" +
                "          sectors.sort(function(a, b) {\n" +
                "            var nameA = (a.sectorName || '').toUpperCase();\n" +
                "            var nameB = (b.sectorName || '').toUpperCase();\n" +
                "            if (nameA === 'SOL') return -1;\n" +
                "            if (nameB === 'SOL') return 1;\n" +
                "            return nameA.localeCompare(nameB);\n" +
                "          });\n" +
                "          var sHtml = '';\n" +
                "          sectors.forEach(function(s) {\n" +
                "            var isSol = (s.sectorName || '').toUpperCase() === 'SOL';\n" +
                "            var sectorId = s.sectorName ? s.sectorName.replace(/\\s+/g, '_') : 'sector';\n" +
                "            var solBadge = isSol ? '<span style=\"color:#00d4ff;font-size:12px;margin-left:8px;\">⊕ Earth Solar System</span>' : '';\n" +
                "            sHtml += '<div class=\"item-card\" style=\"cursor:pointer;\" onclick=\"toggleSector(\\'' + sectorId + '\\')\">';\n" +
                "            sHtml += '<div class=\"item-title\"><h4>' + escapeText(s.sectorName || 'Sector') + solBadge + '</h4></div>';\n" +
                "            sHtml += '<div class=\"item-desc\">Worlds: ' + (s.worldCount || 0) + ' | Hyperlanes: ' + (s.hyperlaneCount || 0) + '</div>';\n" +
                "            if (s.capitalWorld) sHtml += '<div class=\"item-desc\">Capital: ' + escapeText(s.capitalWorld) + '</div>';\n" +
                "            sHtml += '<div class=\"item-desc\" style=\"font-size:11px;color:#888;\">Click to ' + (isSol ? 'explore Sol system' : 'expand sector') + '</div>';\n" +
                "            sHtml += '<div id=\"worlds_' + sectorId + '\" style=\"display:none;margin-top:12px;padding-top:12px;border-top:1px solid #333;\">';\n" +
                "            if (s.worlds && s.worlds.length > 0) {\n" +
                "              sHtml += '<h5 style=\"color:#00d4ff;margin-bottom:8px;\">Subsector Worlds (' + s.worlds.length + ')</h5>';\n" +
                "              s.worlds.forEach(function(w) {\n" +
                "                var isEarth = (w.name || '').toLowerCase().indexOf('earth') >= 0;\n" +
                "                var earthFlag = isEarth ? ' 🌍' : '';\n" +
                "                var capitalFlag = w.capital ? ' 👑' : '';\n" +
                "                var habitableFlag = w.habitable ? ' 🌱' : '';\n" +
                "                sHtml += '<div class=\"item-card\" style=\"margin-bottom:8px;padding:8px;background:#1a1a2e;\">';\n" +
                "                sHtml += '<div class=\"item-title\"><strong>' + escapeText(w.name || 'Unknown') + earthFlag + capitalFlag + habitableFlag + '</strong></div>';\n" +
                "                if (w.uwp) sHtml += '<div class=\"item-desc\">UWP: ' + escapeText(w.uwp) + '</div>';\n" +
                "                if (w.x !== undefined && w.y !== undefined) sHtml += '<div class=\"item-desc\">Subsector: (' + w.x + ',' + w.y + ')</div>';\n" +
                "                if (w.tradeCodes && w.tradeCodes.length > 0) sHtml += '<div class=\"item-desc\">Trade: ' + w.tradeCodes.join(', ') + '</div>';\n" +
                "                if (w.hyperlaneCount > 0) sHtml += '<div class=\"item-desc\">Hyperlanes: ' + w.hyperlaneCount + '</div>';\n" +
                "                sHtml += '</div>';\n" +
                "              });\n" +
                "            } else {\n" +
                "              sHtml += '<div class=\"empty-msg\">World details not available. Generate this sector first.</div>';\n" +
                "            }\n" +
                "            sHtml += '</div></div>';\n" +
                "          });\n" +
                "          sectorsList.innerHTML = sHtml;\n" +
                "        } else {\n" +
                "          sectorsList.innerHTML = '<div class=\"empty-msg\">No sectors generated yet.</div>';\n" +
                "        }\n" +
                "      }).catch(function(e) {\n" +
                "        console.error('[Dashboard] Galaxy load failed:', e);\n" +
                "        document.getElementById('galaxy-loading').style.display = 'none';\n" +
                "        document.getElementById('galaxy-empty').style.display = 'block';\n" +
                "      });\n" +
                "    }\n" +
                "\n" +
                "    function toggleSector(sectorId) {\n" +
                "      var worldDiv = document.getElementById('worlds_' + sectorId);\n" +
                "      if (worldDiv) {\n" +
                "        if (worldDiv.style.display === 'none') {\n" +
                "          worldDiv.style.display = 'block';\n" +
                "        } else {\n" +
                "          worldDiv.style.display = 'none';\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    /* ===== COMBAT ===== */\n" +
                "\n" +
                "    function loadCombat() {\n" +
                "      showPanel('combat-panel');\n" +
                "      document.getElementById('combat-loading').style.display = 'block';\n" +
                "      document.getElementById('combat-types').style.display = 'none';\n" +
                "      document.getElementById('combat-active').style.display = 'none';\n" +
                "      document.getElementById('combat-empty').style.display = 'none';\n" +
                "      var token = localStorage.getItem('accessToken');\n" +
                "      console.log('[Dashboard] Loading combat, token:', token ? 'present' : 'missing');\n" +
                "      var headers = {};\n" +
                "      if (token) headers['Authorization'] = 'Bearer ' + token;\n" +
                "      Promise.all([\n" +
                "        fetch('/api/v1/combat/types', { headers: headers }).then(function(r) {\n" +
                "          if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
                "          return r.json();\n" +
                "        }).catch(function(e) { console.warn('[Dashboard] Combat types failed:', e); return []; }),\n" +
                "        fetch('/api/v1/combat/active', { headers: headers }).then(function(r) {\n" +
                "          if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
                "          return r.json();\n" +
                "        }).catch(function(e) { console.warn('[Dashboard] Active combat failed:', e); return []; })\n" +
                "      ]).then(function(results) {\n" +
                "        allCombatTypes = results[0] || [];\n" +
                "        allActiveCombat = results[1] || [];\n" +
                "        console.log('[Dashboard] Combat loaded - types:', allCombatTypes.length, 'active:', allActiveCombat.length);\n" +
                "        document.getElementById('combat-loading').style.display = 'none';\n" +
                "        if (!allCombatTypes.length && !allActiveCombat.length) {\n" +
                "          document.getElementById('combat-empty').style.display = 'block';\n" +
                "          return;\n" +
                "        }\n" +
                "        // Combat types\n" +
                "        if (allCombatTypes.length) {\n" +
                "          document.getElementById('combat-types').style.display = 'grid';\n" +
                "          var tHtml = '';\n" +
                "          allCombatTypes.forEach(function(t) {\n" +
                "            tHtml += '<div class=\"combat-type-card\"><h4>' + escapeText(t.name) + '</h4><p>' + escapeText(t.description) + '</p></div>';\n" +
                "          });\n" +
                "          document.getElementById('combat-types').innerHTML = tHtml;\n" +
                "        }\n" +
                "        // Active battles\n" +
                "        document.getElementById('combat-active').style.display = 'block';\n" +
                "        var activeList = document.getElementById('combat-active-list');\n" +
                "        var activeEmpty = document.getElementById('combat-active-empty');\n" +
                "        if (allActiveCombat.length) {\n" +
                "          activeEmpty.style.display = 'none';\n" +
                "          var aHtml = '';\n" +
                "          allActiveCombat.forEach(function(a) {\n" +
                "            aHtml += '<div class=\"item-card\"><div class=\"item-title\"><h4>' + escapeText(a.name || 'Battle ' + (a.id||'')) + '</h4>';\n" +
                "            aHtml += '<span class=\"item-source\">' + escapeText(a.source || '') + '</span></div>';\n" +
                "            if (a.status) aHtml += '<div class=\"item-desc\">Status: ' + escapeText(a.status) + '</div>';\n" +
                "            aHtml += '</div>';\n" +
                "          });\n" +
                "          activeList.innerHTML = aHtml;\n" +
                "        } else {\n" +
                "          activeEmpty.style.display = 'block';\n" +
                "          activeList.innerHTML = '';\n" +
                "        }\n" +
                "      }).catch(function(e) {\n" +
                "        console.error('[Dashboard] Combat load failed:', e);\n" +
                "        document.getElementById('combat-loading').style.display = 'none';\n" +
                "        document.getElementById('combat-empty').style.display = 'block';\n" +
                "      });\n" +
                "    }\n" +
                "\n" +
                "    /* ===== GAME NODE STATUS ===== */\n" +
                "\n" +
                "    function loadStatus() {\n" +
                "      showPanel('status-panel');\n" +
                "      var dist = JSON.parse(localStorage.getItem('clientDistribution') || '{}');\n" +
                "      var details = [\n" +
                "        { label: 'Platform', value: dist.platform || 'Unknown' },\n" +
                "        { label: 'Client Type', value: dist.clientType || 'Unknown' },\n" +
                "        { label: 'Connection', value: 'Connected', cls: 'online' },\n" +
                "        { label: 'Game Engine', value: 'Ready' },\n" +
                "        { label: 'AI Models', value: (dist.aiModels || []).length + ' loaded' },\n" +
                "        { label: 'Game Rules', value: (dist.gameRules || []).length + ' systems' },\n" +
                "        { label: 'Sync Endpoint', value: dist.syncEndpoint || 'N/A' },\n" +
                "        { label: 'Last Sync', value: document.getElementById('sync-status').textContent }\n" +
                "      ];\n" +
                "      var html = '';\n" +
                "      details.forEach(function(d) {\n" +
                "        html += '<div class=\"detail-item\"><div class=\"detail-label\">' + d.label + '</div>';\n" +
                "        html += '<div class=\"detail-value' + (d.cls ? ' ' + d.cls : '') + '\">' + escapeText(d.value) + '</div></div>';\n" +
                "      });\n" +
                "      document.getElementById('status-details').innerHTML = html;\n" +
                 "    }\n" +
                 "\n" +
                 "    /* ===== EARTH 2026 ===== */\n" +
                 "\n" +
                 "    function loadEarth2026() {\n" +
                 "      showPanel('earth2026-panel');\n" +
                 "      document.getElementById('earth2026-loading').style.display = 'block';\n" +
                 "      document.getElementById('earth2026-content').style.display = 'none';\n" +
                 "      document.getElementById('earth2026-empty').style.display = 'none';\n" +
                 "      document.getElementById('e26-char-result').innerHTML = '';\n" +
                 "      var token = localStorage.getItem('accessToken');\n" +
                 "      console.log('[Dashboard] Loading Earth 2026, token:', token ? 'present' : 'missing');\n" +
                 "      var headers = {};\n" +
                 "      if (token) headers['Authorization'] = 'Bearer ' + token;\n" +
                 "      // Get username from stored data or platform\n" +
                 "      var dist = JSON.parse(localStorage.getItem('clientDistribution') || '{}');\n" +
                 "      earth2026Username = dist.platform || 'Commander';\n" +
                 "      Promise.all([\n" +
                 "        fetch('/api/v1/earth2026/locations', { headers: headers }).then(function(r) {\n" +
                 "          if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
                 "          return r.json();\n" +
                 "        }).catch(function(e) { console.warn('[Dashboard] Earth 2026 locations failed:', e); return []; }),\n" +
                 "        fetch('/api/v1/earth2026/first-scenario/1', { headers: headers }).then(function(r) {\n" +
                 "          if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
                 "          return r.json();\n" +
                 "        }).catch(function(e) { console.warn('[Dashboard] Earth 2026 scenarios failed:', e); return null }),\n" +
                 "        fetch('/api/v1/earth2026/starting-location', { headers: headers }).then(function(r) {\n" +
                 "          if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
                 "          return r.json();\n" +
                 "        }).catch(function(e) { console.warn('[Dashboard] Earth 2026 starting location failed:', e); return null }),\n" +
                 "        fetch('/api/v1/4x/sectors', { headers: headers }).then(function(r) {\n" +
                 "          if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
                 "          return r.json();\n" +
                 "        }).catch(function(e) { console.warn('[Dashboard] Sectors failed:', e); return []; })\n" +
                 "      ]).then(function(results) {\n" +
                 "        allEarth2026Locations = results[0] || [];\n" +
                 "        var scenario = results[1];\n" +
                 "        var startingLocation = results[2];\n" +
                 "        var sectors = results[3] || [];\n" +
                 "        allEarth2026Scenarios = scenario ? [scenario] : [];\n" +
                 "        console.log('[Dashboard] Earth 2026 loaded - locations:', allEarth2026Locations.length, 'scenarios:', allEarth2026Scenarios.length, 'sectors:', sectors.length);\n" +
                 "        document.getElementById('earth2026-loading').style.display = 'none';\n" +
                 "        document.getElementById('earth2026-content').style.display = 'block';\n" +
                 "        // Find Sol System sector\n" +
                 "        var solSector = null;\n" +
                 "        if (sectors && sectors.length > 0) {\n" +
                 "          for (var i = 0; i < sectors.length; i++) {\n" +
                 "            if (sectors[i].sectorName && sectors[i].sectorName.toUpperCase() === 'SOL') {\n" +
                 "              solSector = sectors[i];\n" +
                 "              break;\n" +
                 "            }\n" +
                 "          }\n" +
                 "        }\n" +
                 "        // Render Sol System panel\n" +
                 "        var solPanel = document.getElementById('sol-system-panel');\n" +
                 "        var solWorldsDiv = document.getElementById('sol-system-worlds');\n" +
                 "        if (solSector && solSector.worlds && solSector.worlds.length > 0) {\n" +
                 "          solPanel.style.display = 'block';\n" +
                 "          var solHtml = '';\n" +
                 "          solSector.worlds.forEach(function(w) {\n" +
                 "            var isEarth = (w.name || '').toLowerCase().indexOf('earth') >= 0;\n" +
                 "            var isCapital = w.capital;\n" +
                 "            var earthFlag = isEarth ? ' 🌍' : '';\n" +
                 "            var capitalFlag = isCapital ? ' 👑' : '';\n" +
                 "            var habitableFlag = w.habitable ? ' 🌱' : '';\n" +
                 "            var style = isEarth ? 'border:1px solid #00d4ff;background:#1a2a3a;' : 'border:1px solid #1e2840;';\n" +
                 "            solHtml += '<div class=\"item-card\" style=\"margin-bottom:8px;padding:8px;' + style + '\">';\n" +
                 "            solHtml += '<div class=\"item-title\"><strong>' + escapeText(w.name || 'Unknown') + earthFlag + capitalFlag + habitableFlag + '</strong></div>';\n" +
                 "            if (w.uwp) solHtml += '<div class=\"item-desc\">UWP: ' + escapeText(w.uwp) + '</div>';\n" +
                 "            if (w.x !== undefined && w.y !== undefined) solHtml += '<div class=\"item-desc\">Position: (' + w.x + ',' + w.y + ')</div>';\n" +
                 "            if (isEarth) solHtml += '<div class=\"item-desc\" style=\"color:#4caf50;\">⊕ Home World - Landing Available</div>';\n" +
                 "            solHtml += '</div>';\n" +
                 "          });\n" +
                 "          solWorldsDiv.innerHTML = solHtml;\n" +
                 "        } else {\n" +
                 "          solPanel.style.display = 'block';\n" +
                 "          solWorldsDiv.innerHTML = '<div class=\"empty-msg\">Sol System data loading... Earth is your landing target.</div>';\n" +
                 "        }\n" +
                 "        // Starting location info\n" +
                 "        var infoHtml = '';\n" +
                 "        if (startingLocation) {\n" +
                 "          infoHtml += '<h4>📍 Your Starting Location - Sol System</h4>';\n" +
                 "          if (startingLocation.name) infoHtml += '<p><b>' + escapeText(startingLocation.name) + '</b></p>';\n" +
                 "          if (startingLocation.description) infoHtml += '<p>' + escapeText(startingLocation.description) + '</p>';\n" +
                 "          if (startingLocation.city) infoHtml += '<p>City: ' + escapeText(startingLocation.city) + '</p>';\n" +
                 "          if (startingLocation.country) infoHtml += '<p>Country: ' + escapeText(startingLocation.country) + '</p>';\n" +
                 "        } else {\n" +
                 "          infoHtml += '<h4>📍 Sol System - Earth Landing</h4>';\n" +
                 "          infoHtml += '<p>You have arrived in the <b>Sol System</b> - Earth\\'s home solar system.</p>';\n" +
                 "          infoHtml += '<p>🌍 <b>Earth</b> is your landing target. San Diego, California awaits.</p>';\n" +
                 "          infoHtml += '<p>You arrive at Younger Industries HQ in San Diego. Your apartment is ready and your first scenario awaits.</p>';\n" +
                 "        }\n" +
                 "        document.getElementById('earth2026-info').innerHTML = infoHtml;\n" +
                 "        // Locations\n" +
                 "        var locList = document.getElementById('earth2026-locations');\n" +
                 "        var locEmpty = document.getElementById('earth2026-locations-empty');\n" +
                 "        if (allEarth2026Locations.length > 0) {\n" +
                 "          locEmpty.style.display = 'none';\n" +
                 "          var lHtml = '';\n" +
                 "          allEarth2026Locations.forEach(function(loc) {\n" +
                 "            var locId = loc.id != null ? loc.id : loc.name;\n" +
                 "            var locIdStr = typeof locId === 'number' ? String(locId) : escapeText(locId);\n" +
                 "            lHtml += '<div class=\"item-card\"><div class=\"item-title\"><h4>' + escapeText(loc.name || 'Unknown Location') + '</h4></div>';\n" +
                 "            if (loc.type) lHtml += '<div class=\"item-desc\">Type: ' + escapeText(loc.type) + '</div>';\n" +
                 "            if (loc.description) lHtml += '<div class=\"item-desc\">' + escapeText(loc.description) + '</div>';\n" +
                 "            lHtml += '<div id=\"land-result-' + locIdStr + '\" style=\"margin:8px 0;min-height:20px;\"></div>';\n" +
                 "            lHtml += '<div style=\"margin-top:4px;\"><button class=\"action\" id=\"land-btn-' + locIdStr + '\" onclick=\"landAtLocation(' + locIdStr + ', \\'' + locIdStr + '\\', \\'' + escapeText(loc.name) + '\\')\">🚀 Land Here</button></div>';\n" +
                 "            lHtml += '</div>';\n" +
                 "          });\n" +
                 "          locList.innerHTML = lHtml;\n" +
                 "        } else {\n" +
                 "          locEmpty.style.display = 'block';\n" +
                 "          locList.innerHTML = '';\n" +
                 "        }\n" +
                 "        // Scenarios\n" +
                 "        var scenList = document.getElementById('earth2026-scenarios');\n" +
                 "        var scenEmpty = document.getElementById('earth2026-scenarios-empty');\n" +
                 "        if (allEarth2026Scenarios.length > 0) {\n" +
                 "          scenEmpty.style.display = 'none';\n" +
                 "          var sHtml = '';\n" +
                 "          allEarth2026Scenarios.forEach(function(sc) {\n" +
                 "            sHtml += '<div class=\"item-card\"><div class=\"item-title\"><h4>' + escapeText(sc.name || 'Scenario') + '</h4></div>';\n" +
                 "            if (sc.description) sHtml += '<div class=\"item-desc\">' + escapeText(sc.description) + '</div>';\n" +
                 "            if (sc.level) sHtml += '<div class=\"item-desc\">Level: ' + sc.level + '</div>';\n" +
                 "            sHtml += '</div>';\n" +
                 "          });\n" +
                 "          scenList.innerHTML = sHtml;\n" +
                 "        } else {\n" +
                 "          scenEmpty.style.display = 'block';\n" +
                 "          scenList.innerHTML = '';\n" +
                 "        }\n" +
                 "      }).catch(function(e) {\n" +
                 "        console.error('[Dashboard] Earth 2026 load failed:', e);\n" +
                 "        document.getElementById('earth2026-loading').style.display = 'none';\n" +
                 "        document.getElementById('earth2026-empty').style.display = 'block';\n" +
                 "      });\n" +
                 "    }\n" +
                 "\n" +
                 "    function createEarthCharacter() {\n" +
                 "      var name = document.getElementById('e26-char-name').value.trim();\n" +
                 "      var age = parseInt(document.getElementById('e26-char-age').value) || 25;\n" +
                 "      var occupation = document.getElementById('e26-char-occupation').value.trim();\n" +
                 "      var resultDiv = document.getElementById('e26-char-result');\n" +
                 "      if (!name) {\n" +
                 "        resultDiv.innerHTML = '<div style=\"color:#ff6b6b;\">Character name is required.</div>';\n" +
                 "        return;\n" +
                 "      }\n" +
                 "      var token = localStorage.getItem('accessToken');\n" +
                 "      var headers = { 'Content-Type': 'application/json' };\n" +
                 "      if (token) headers['Authorization'] = 'Bearer ' + token;\n" +
                 "      resultDiv.innerHTML = '<div style=\"color:#9aa0a6;\">Creating character...</div>';\n" +
                 "      var body = JSON.stringify({\n" +
                 "        name: name,\n" +
                 "        age: age,\n" +
                 "        occupation: occupation || null,\n" +
                 "        username: earth2026Username\n" +
                 "      });\n" +
                 "      fetch('/api/v1/earth2026/characters', { method: 'POST', headers: headers, body: body })\n" +
                 "        .then(function(r) {\n" +
                 "          if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
                 "          return r.json();\n" +
                 "        })\n" +
                 "        .then(function(data) {\n" +
                 "          console.log('[Dashboard] Character created:', data);\n" +
                 "          var msg = '<div style=\"color:#4caf50;\">✓ Character <b>' + escapeText(name) + '</b> created! ID: ' + (data.id || 'unknown') + '</div>';\n" +
                 "          msg += '<div style=\"color:#9aa0a6;margin-top:8px;\">You have landed on Earth 2026 in San Diego, CA. Your apartment at Younger Industries HQ is ready.</div>';\n" +
                 "          if (data.location) msg += '<div style=\"color:#00d4ff;margin-top:4px;\">Location: ' + escapeText(data.location) + '</div>';\n" +
                 "          resultDiv.innerHTML = msg;\n" +
                 "          // Try to start the game for this character\n" +
                 "          if (data.id) {\n" +
                 "            fetch('/api/v1/earth2026/characters/' + data.id + '/start-game', { method: 'POST', headers: headers })\n" +
                 "              .then(function(r) { return r.json(); })\n" +
                 "              .then(function(startData) {\n" +
                 "                console.log('[Dashboard] Game started:', startData);\n" +
                 "                if (startData.message) {\n" +
                 "                  resultDiv.innerHTML += '<div style=\"color:#4caf50;margin-top:8px;\">' + escapeText(startData.message) + '</div>';\n" +
                 "                }\n" +
                 "              })\n" +
                 "              .catch(function(e) { console.warn('[Dashboard] Start game failed:', e); });\n" +
                 "          }\n" +
                 "        })\n" +
                 "        .catch(function(e) {\n" +
                 "          console.error('[Dashboard] Create character failed:', e);\n" +
                 "          resultDiv.innerHTML = '<div style=\"color:#ff6b6b;\">Failed to create character: ' + escapeText(e.message) + '</div>';\n" +
                 "        });\n" +
                 "    }\n" +
                 "\n" +
                 "    function landAtLocation(locationId, resultId, locName) {\n" +
                 "      console.log('[Dashboard] landAtLocation called:', locationId, resultId, locName);\n" +
                 "      var token = localStorage.getItem('accessToken');\n" +
                 "      var headers = { 'Content-Type': 'application/json' };\n" +
                 "      if (token) headers['Authorization'] = 'Bearer ' + token;\n" +
                 "      // Show result inline next to the button\n" +
                 "      var resultDiv = document.getElementById('land-result-' + (resultId || ''));\n" +
                 "      var btn = document.getElementById('land-btn-' + (resultId || ''));\n" +
                 "      if (resultDiv) {\n" +
                 "        resultDiv.innerHTML = '<div style=\"color:#9aa0a6;\">⏳ Landing...</div>';\n" +
                 "      }\n" +
                 "      if (btn) {\n" +
                 "        btn.disabled = true;\n" +
                 "        btn.textContent = '⏳ Landing...';\n" +
                 "      }\n" +
                 "      // Determine if locationId is numeric or a name\n" +
                 "      var parsedId = parseInt(locationId);\n" +
                 "      var bodyObj = isNaN(parsedId) ? { locationId: locationId } : { locationId: parsedId };\n" +
                 "      var body = JSON.stringify(bodyObj);\n" +
                 "      console.log('[Dashboard] Sending land request:', body);\n" +
                 "      fetch('/api/v1/earth2026/characters/land', { method: 'POST', headers: headers, body: body })\n" +
                 "        .then(function(r) {\n" +
                 "          if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
                 "          return r.json();\n" +
                 "        })\n" +
                 "        .then(function(data) {\n" +
                 "          console.log('[Dashboard] Landed at location:', data);\n" +
                 "          var name = locName || data.location || 'Unknown';\n" +
                 "          var msg = '<div style=\"color:#4caf50;\">✓ Landed at <b>' + escapeText(name) + '</b>!</div>';\n" +
                 "          if (data.message) msg += '<div style=\"color:#00d4ff;margin-top:4px;\">' + escapeText(data.message) + '</div>';\n" +
                 "          if (data.characterName) msg += '<div style=\"color:#9aa0a6;margin-top:4px;\">Character: ' + escapeText(data.characterName) + '</div>';\n" +
                 "          if (resultDiv) resultDiv.innerHTML = msg;\n" +
                 "          if (btn) { btn.disabled = false; btn.textContent = '✓ Landed'; }\n" +
                 "          // Also update the character creation result area\n" +
                 "          var e26Result = document.getElementById('e26-char-result');\n" +
                 "          if (e26Result) e26Result.innerHTML = msg;\n" +
                 "          // Try to start the first scenario after landing\n" +
                  "          if (data.characterId) {\n" +
                  "            fetch('/api/v1/earth2026/characters/' + data.characterId + '/start-game', { method: 'POST', headers: headers })\n" +
                  "              .then(function(r) { return r.json(); })\n" +
                  "              .then(function(startData) {\n" +
                  "                console.log('[Dashboard] Game started after landing:', startData);\n" +
                  "                if (startData.message && resultDiv) {\n" +
                  "                  resultDiv.innerHTML += '<div style=\"color:#4caf50;margin-top:8px;\">' + escapeText(startData.message) + '</div>';\n" +
                  "                }\n" +
                  "                // Navigate to the game HUD page after successful landing\n" +
                  "                console.log('[Dashboard] Navigating to game HUD...');\n" +
                  "                localStorage.setItem('earth2026CharacterId', data.characterId);\n" +
                  "                localStorage.setItem('earth2026Location', name);\n" +
                  "                setTimeout(function() {\n" +
                  "                  window.location.href = '/earth2026/';\n" +
                  "                }, 1000);\n" +
                  "              })\n" +
                  "              .catch(function(e) {\n" +
                  "                console.warn('[Dashboard] Start game after landing failed:', e);\n" +
                  "                // Still navigate to game HUD even if start-game fails\n" +
                  "                console.log('[Dashboard] Navigating to game HUD after start-game failure...');\n" +
                  "                localStorage.setItem('earth2026CharacterId', data.characterId);\n" +
                  "                localStorage.setItem('earth2026Location', name);\n" +
                  "                setTimeout(function() {\n" +
                  "                  window.location.href = '/earth2026/';\n" +
                  "                }, 1500);\n" +
                  "              });\n" +
                  "          } else {\n" +
                  "            // No characterId returned, still navigate to game HUD\n" +
                  "            console.log('[Dashboard] No characterId, navigating to game HUD...');\n" +
                  "            localStorage.setItem('earth2026Location', name);\n" +
                  "            setTimeout(function() {\n" +
                  "              window.location.href = '/earth2026/';\n" +
                  "            }, 1500);\n" +
                  "          }\n" +
                 "        })\n" +
                 "        .catch(function(e) {\n" +
                 "          console.error('[Dashboard] Land at location failed:', e);\n" +
                 "          var errMsg = '<div style=\"color:#ff6b6b;\">Failed to land: ' + escapeText(e.message) + '</div>';\n" +
                 "          errMsg += '<div style=\"color:#9aa0a6;font-size:0.85em;margin-top:4px;\">If you haven\\'t created a character yet, use the form above first.</div>';\n" +
                 "          if (resultDiv) resultDiv.innerHTML = errMsg;\n" +
                 "          if (btn) { btn.disabled = false; btn.textContent = '🚀 Land Here'; }\n" +
                 "          var e26Result = document.getElementById('e26-char-result');\n" +
                 "          if (e26Result) e26Result.innerHTML = errMsg;\n" +
                 "        });\n" +
                 "    }\n" +
                 "\n" +
                 "    /* ===== GENERIC RENDERER ===== */\n" +
                "\n" +
                "    function renderSectionItems(section, items, fields) {\n" +
                "      var listId = section + '-list';\n" +
                "      var emptyId = section + '-empty';\n" +
                "      var list = document.getElementById(listId);\n" +
                "      var empty = document.getElementById(emptyId);\n" +
                "      if (!items || items.length === 0) {\n" +
                "        list.innerHTML = '';\n" +
                "        empty.style.display = 'block';\n" +
                "        return;\n" +
                "      }\n" +
                "      empty.style.display = 'none';\n" +
                "      var html = '';\n" +
                "      items.forEach(function(item) {\n" +
                "        html += '<div class=\"item-card\">';\n" +
                "        html += '  <div class=\"item-title\">';\n" +
                "        html += '    <h4>' + escapeText(item.name || 'Unnamed') + '</h4>';\n" +
                "        if (item.source) html += '    <span class=\"item-source\">' + escapeText(item.source) + '</span>';\n" +
                "        html += '  </div>';\n" +
                "        if (item.description) html += '  <div class=\"item-desc\">' + escapeText(item.description) + '</div>';\n" +
                "        html += '  <div class=\"item-meta\">';\n" +
                "        fields.forEach(function(f) {\n" +
                "          if (item[f]) html += '    <div class=\"meta-item\"><span class=\"meta-label\">' + f + '</span><span class=\"meta-value\">' + escapeText(String(item[f])) + '</span></div>';\n" +
                "        });\n" +
                "        html += '  </div>';\n" +
                "        html += '</div>';\n" +
                "      });\n" +
                "      list.innerHTML = html;\n" +
                "    }\n" +
                "\n" +
                "    /* ===== INIT ===== */\n" +
                "\n" +
                "    (function() {\n" +
                "      var token = localStorage.getItem('accessToken');\n" +
                "      if (!token) {\n" +
                "        console.warn('[Dashboard] No access token found, redirecting to login');\n" +
                "        window.location.href = '/';\n" +
                "        return;\n" +
                "      }\n" +
                "      console.log('[Dashboard] Initializing with token present');\n" +
                "      var headers = { 'Authorization': 'Bearer ' + token };\n" +
                "      fetch('/api/v1/player/platform', { headers: headers })\n" +
                "        .then(function(r) {\n" +
                "          if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
                "          return r.json();\n" +
                "        })\n" +
                "        .then(function(data) {\n" +
                "          console.log('[Dashboard] Platform data loaded:', data.platform);\n" +
                "          localStorage.setItem('clientDistribution', JSON.stringify(data));\n" +
                "          document.getElementById('player-name').textContent = data.platform || 'Commander';\n" +
                "          document.getElementById('engine-status').textContent = 'Ready';\n" +
                "          document.getElementById('ai-status').textContent = 'Loaded';\n" +
                "          document.getElementById('sync-status').textContent = 'Just now';\n" +
                "        })\n" +
                "        .catch(function(e) {\n" +
                "          console.error('[Dashboard] Failed to load platform data:', e);\n" +
                "          document.getElementById('engine-status').textContent = 'Error';\n" +
                "          document.getElementById('ai-status').textContent = 'Error';\n" +
                "        });\n" +
                "    })();\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Redirect to the default game server.
     */
    @GetMapping("/game")
    public ResponseEntity<?> redirectToGame(HttpServletRequest request) {
        String target = resolveDefaultGameUrl();
        log.debug("Redirecting to default game server: {}", target);
        return ResponseEntity.status(302)
                .location(URI.create(target))
                .build();
    }

    /**
     * Catch-all for /game/** paths — routes to the appropriate game server.
     *
     * Supports multi-game routing: /game/{gameId}/** → game-specific server
     * Falls back to default game server when no gameId is found.
     */
    @GetMapping("/game/**")
    public ResponseEntity<?> proxyToGame(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String gameId = gameRouter.resolveGameId(requestPath);
        String queryString = request.getQueryString();

        String target = gameRouter.buildTargetUrl(gameId, requestPath, queryString);
        log.debug("Routing {} (game: {}) to {}", requestPath, gameId, target);
        return ResponseEntity.status(302)
                .location(URI.create(target))
                .build();
    }

    /**
     * Resolve the default game server URL.
     */
    private String resolveDefaultGameUrl() {
        Optional<GameProfile> defaultGame = gameRegistry.getDefaultGame();
        if (defaultGame.isPresent() && defaultGame.get().getGameServerUrl() != null) {
            String url = defaultGame.get().getGameServerUrl();
            return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        }
        // Fallback to config
        String url = config.getGameServerUrl();
        if (url == null || url.isEmpty()) {
            url = config.getCoordinatorUrl();
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /**
     * Earth 2026 game entry page — launched from the character panel.
     */
    @GetMapping({"/earth2026/", "/earth2026"})
    public ResponseEntity<String> earth2026GamePage(HttpServletRequest request) {
        String characterId = request.getParameter("characterId");
        String charName = characterId != null ? "Character " + escapeHtml(characterId) : "Player";

        String html = "<!DOCTYPE html>\n" +
                "<html lang='en'>\n" +
                "<head>\n" +
                "  <meta charset='UTF-8'>\n" +
                "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "  <title>Earth 2026 - " + charName + "</title>\n" +
                "  <style>\n" +
                "    * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "    body { background: #0a0a1a; color: #e0e0e0; font-family: 'Segoe UI', system-ui, sans-serif; min-height: 100vh; }\n" +
                "    .header { background: #131926; padding: 16px 24px; border-bottom: 1px solid #1e2840; display: flex; justify-content: space-between; align-items: center; }\n" +
                "    .header h1 { color: #00d4ff; font-size: 1.2em; }\n" +
                "    .header a { color: #9aa0a6; text-decoration: none; font-size: 0.85em; }\n" +
                "    .header a:hover { color: #00d4ff; }\n" +
                "    .container { max-width: 960px; margin: 32px auto; padding: 0 24px; }\n" +
                "    .welcome { background: #131926; padding: 24px; border-radius: 10px; border: 1px solid #1e2840; margin-bottom: 24px; }\n" +
                "    .welcome h2 { color: #00d4ff; margin-bottom: 8px; }\n" +
                "    .welcome p { color: #9aa0a6; line-height: 1.6; }\n" +
                "    .game-area { background: #131926; padding: 24px; border-radius: 10px; border: 1px solid #1e2840; min-height: 400px; }\n" +
                "    .game-area h3 { color: #00d4ff; margin-bottom: 16px; }\n" +
                "    .game-area p { color: #9aa0a6; line-height: 1.6; margin-bottom: 12px; }\n" +
                "    .scene { padding: 16px 0; border-bottom: 1px solid #1e2840; margin-bottom: 16px; }\n" +
                "    .scene:last-child { border-bottom: none; }\n" +
                "    .scene-narrative { color: #9aa0a6; line-height: 1.8; margin-bottom: 16px; font-size: 0.95em; }\n" +
                "    .scene-choices { display: flex; flex-direction: column; gap: 8px; margin-top: 12px; }\n" +
                "    .choice-btn { display: inline-block; background: #1a2332; color: #00d4ff; padding: 12px 20px; border-radius: 6px; border: 1px solid #1e2840; font-size: 0.9em; cursor: pointer; text-align: left; transition: all 0.2s; }\n" +
                "    .choice-btn:hover { background: #243044; border-color: #00d4ff; }\n" +
                "    .action-btn { display: inline-block; background: #00d4ff; color: #0a0a1a; padding: 10px 20px; border-radius: 6px; text-decoration: none; font-size: 0.9em; font-weight: 600; cursor: pointer; border: none; margin-top: 8px; }\n" +
                "    .action-btn:hover { background: #00b8d4; }\n" +
                "    .action-btn:disabled { background: #333; color: #666; cursor: not-allowed; }\n" +
                "    .scenario-card { background: #1a2332; padding: 16px; border-radius: 8px; border: 1px solid #1e2840; margin-bottom: 12px; }\n" +
                "    .scenario-card h4 { color: #00d4ff; margin-bottom: 8px; font-size: 1em; }\n" +
                "    .scenario-card p { color: #9aa0a6; line-height: 1.5; margin-bottom: 8px; font-size: 0.9em; }\n" +
                "    .scenario-meta { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 12px; }\n" +
                "    .meta-tag { background: #0a0a1a; color: #9aa0a6; padding: 4px 10px; border-radius: 4px; font-size: 0.75em; text-transform: uppercase; }\n" +
                "    .meta-tag.difficulty { color: #ff9800; }\n" +
                "    .meta-tag.type { color: #4caf50; }\n" +
                "    .meta-tag.xp { color: #e040fb; }\n" +
                "    .hidden { display: none; }\n" +
                "    .fade-in { animation: fadeIn 0.5s ease-in; }\n" +
                "    @keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }\n" +
                "    .status-bar { margin-top: 24px; padding: 16px 20px; background: #131926; border-radius: 10px; border: 1px solid #1e2840; }\n" +
                "    .status-bar h4 { color: #9aa0a6; font-size: 0.85em; margin-bottom: 8px; text-transform: uppercase; }\n" +
                "    .status { display: flex; gap: 24px; flex-wrap: wrap; }\n" +
                "    .status-item { display: flex; flex-direction: column; gap: 4px; }\n" +
                "    .label { color: #9aa0a6; font-size: 0.75em; text-transform: uppercase; }\n" +
                "    .value { color: #e0e0e0; font-size: 0.9em; }\n" +
                "    .online { color: #4caf50; }\n" +
                "    .loading { color: #9aa0a6; text-align: center; padding: 40px; }\n" +
                "    .loading::after { content: '...'; animation: dots 1.5s infinite; }\n" +
                "    @keyframes dots { 0%, 20% { content: '.'; } 40% { content: '..'; } 60%, 100% { content: '...'; } }\n" +
                "    .result-msg { background: #1a2332; padding: 16px; border-radius: 8px; border-left: 3px solid #00d4ff; margin-top: 12px; }\n" +
                "    .result-msg.success { border-left-color: #4caf50; }\n" +
                "    .result-msg.error { border-left-color: #f44336; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class='header'>\n" +
                "    <h1>🌍 Earth 2026</h1>\n" +
                "    <a href='/corporation/'>← Back to Dashboard</a>\n" +
                "  </div>\n" +
                "  <div class='container'>\n" +
                "    <div class='welcome'>\n" +
                "      <h2>☀️ Sol System - Earth Landing, " + charName + "!</h2>\n" +
                "      <p>You have arrived in the <b>Sol System</b> - Earth's home solar system. Your ship descends through Earth's atmosphere toward San Diego, California.</p>\n" +
                "      <p>You've been recruited by Younger Industries for a mission that could change everything.</p>\n" +
                "    </div>\n" +
                "    <div class='game-area' id='gameArea'>\n" +
                "      <div class='loading' id='loadingMsg'>Loading your adventure</div>\n" +
                "    </div>\n" +
                "    <div class='status-bar'>\n" +
                "      <h4>Game Node Status</h4>\n" +
                "      <div class='status'>\n" +
                "        <div class='status-item'><span class='label'>Connection</span><span class='value online'>● Connected</span></div>\n" +
                "        <div class='status-item'><span class='label'>Game Engine</span><span class='value'>Running</span></div>\n" +
                "        <div class='status-item'><span class='label'>AI Model</span><span class='value'>earth2026-ai v1</span></div>\n" +
                "        <div class='status-item'><span class='label'>Character</span><span class='value'>" + charName + "</span></div>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "  <script>\n" +
                "    var token = localStorage.getItem('accessToken');\n" +
                "    var scenarios = [];\n" +
                "    var currentScene = 'intro';\n" +
                "\n" +
                "    /* ===== SCENE DEFINITIONS ===== */\n" +
                "\n" +
                "    var scenes = {\n" +
                "      intro: {\n" +
                "        title: '📍 San Diego Convention Center',\n" +
                "        narrative: 'You stand in the bustling convention center, surrounded by booths and crowds. The air hums with conversation and the clatter of footsteps. A man in a dark suit approaches you with an envelope.',\n" +
                "        dialogue: '\"You\\'ve been selected. Open the envelope and follow the instructions.\"',\n" +
                "        choices: [\n" +
                "          { text: '📧 Open the Envelope', next: 'envelope' },\n" +
                "          { text: '👀 Look around first', next: 'look_around' }\n" +
                "        ]\n" +
                "      },\n" +
                "      look_around: {\n" +
                "        title: '📍 San Diego Convention Center',\n" +
                "        narrative: 'You glance around the convention center. Booths display the latest tech gadgets and corporate sponsorships. Younger Industries has a prominent booth near the entrance. The man in the dark suit is still waiting, patient but insistent.',\n" +
                "        dialogue: '\"I don\\'t have much time. Take the envelope.\"',\n" +
                "        choices: [\n" +
                "          { text: '📧 Take the Envelope', next: 'envelope' },\n" +
                "          { text: '❓ Ask who he is', next: 'ask_who' }\n" +
                "        ]\n" +
                "      },\n" +
                "      ask_who: {\n" +
                "        title: '📍 San Diego Convention Center',\n" +
                "        narrative: 'The man hesitates, then hands you a business card along with the envelope. It reads: \"Agent K. Younger Industries Security.\" He nods once and melts into the crowd.',\n" +
                "        dialogue: 'The envelope feels heavy in your hand. Whatever is inside, it\\'s important.',\n" +
                "        choices: [\n" +
                "          { text: '📧 Open the Envelope', next: 'envelope' }\n" +
                "        ]\n" +
                "      },\n" +
                "      envelope: {\n" +
                "        title: '📧 The Envelope',\n" +
                "        narrative: 'Inside the envelope, you find a letter on Younger Industries letterhead:',\n" +
                "        dialogue: '\"Congratulations. You have been chosen for a special assignment. Your skills and background make you the perfect candidate. Below are the available missions. Choose wisely — your decision will determine your path forward.\"',\n" +
                "        choices: [\n" +
                "          { text: '📋 View Available Missions', next: 'missions' }\n" +
                "        ]\n" +
                "      },\n" +
                "      missions: {\n" +
                "        title: '📋 Available Missions',\n" +
                "        narrative: 'The letter lists several active missions. Each has a different risk level and reward. Choose your path:',\n" +
                "        dynamic: true\n" +
                "      }\n" +
                "    };\n" +
                "\n" +
                "    /* ===== RENDER ENGINE ===== */\n" +
                "\n" +
                "    function renderScene(sceneId) {\n" +
                "      var scene = scenes[sceneId];\n" +
                "      if (!scene) {\n" +
                "        console.error('[Game] Unknown scene:', sceneId);\n" +
                "        return;\n" +
                "      }\n" +
                "      currentScene = sceneId;\n" +
                "      var area = document.getElementById('gameArea');\n" +
                "\n" +
                "      if (sceneId === 'missions') {\n" +
                "        renderMissionsScene(area);\n" +
                "        return;\n" +
                "      }\n" +
                "\n" +
                "      var html = '<div class=\"scene fade-in\">';\n" +
                "      html += '<h3>' + scene.title + '</h3>';\n" +
                "      html += '<p class=\"scene-narrative\">' + scene.narrative + '</p>';\n" +
                "      if (scene.dialogue) {\n" +
                "        html += '<p class=\"scene-narrative\"><em>' + scene.dialogue + '</em></p>';\n" +
                "      }\n" +
                "      html += '<div class=\"scene-choices\">';\n" +
                "      for (var i = 0; i < scene.choices.length; i++) {\n" +
                "        var c = scene.choices[i];\n" +
                "        html += '<button class=\"choice-btn\" onclick=\"renderScene(\\'' + c.next + '\\')\">' + c.text + '</button>';\n" +
                "      }\n" +
                "      html += '</div>';\n" +
                "      html += '</div>';\n" +
                "      area.innerHTML = html;\n" +
                "      console.log('[Game] Rendered scene:', sceneId);\n" +
                "    }\n" +
                "\n" +
                "    function renderMissionsScene(area) {\n" +
                "      var html = '<div class=\"scene fade-in\">';\n" +
                "      html += '<h3>📋 Available Missions</h3>';\n" +
                "      html += '<p class=\"scene-narrative\">The letter lists several active missions. Choose your path:</p>';\n" +
                "\n" +
                "      if (scenarios.length === 0) {\n" +
                "        html += '<p class=\"scene-narrative\">No missions are currently available. Check back later.</p>';\n" +
                "        html += '<div class=\"scene-choices\">';\n" +
                "        html += '<button class=\"choice-btn\" onclick=\"renderScene(\\'intro\\')\">↩ Return to Start</button>';\n" +
                "        html += '</div>';\n" +
                "      } else {\n" +
                "        for (var i = 0; i < scenarios.length; i++) {\n" +
                "          var s = scenarios[i];\n" +
                "          html += '<div class=\"scenario-card\">';\n" +
                "          html += '<h4>' + escapeText(s.name || 'Unknown Mission') + '</h4>';\n" +
                "          html += '<p>' + escapeText(s.description || 'No description available.') + '</p>';\n" +
                "          html += '<div class=\"scenario-meta\">';\n" +
                "          if (s.type) html += '<span class=\"meta-tag type\">' + escapeText(s.type) + '</span>';\n" +
                "          if (s.difficulty) html += '<span class=\"meta-tag difficulty\">Difficulty: ' + s.difficulty + '</span>';\n" +
                "          if (s.xpReward) html += '<span class=\"meta-tag xp\">' + s.xpReward + ' XP</span>';\n" +
                "          if (s.durationMinutes) html += '<span class=\"meta-tag\">~' + s.durationMinutes + ' min</span>';\n" +
                "          html += '</div>';\n" +
                "          html += '<button class=\"action-btn\" onclick=\"startScenario(\\'' + (s.id || '') + '\\')\">▶ Start Mission</button>';\n" +
                "          html += '</div>';\n" +
                "        }\n" +
                "        html += '<div class=\"scene-choices\" style=\"margin-top:16px\">';\n" +
                "        html += '<button class=\"choice-btn\" onclick=\"renderScene(\\'intro\\')\">↩ Return to Start</button>';\n" +
                "        html += '</div>';\n" +
                "      }\n" +
                "      html += '</div>';\n" +
                "      area.innerHTML = html;\n" +
                "    }\n" +
                "\n" +
                "    function showResult(type, message) {\n" +
                "      var area = document.getElementById('gameArea');\n" +
                "      var div = document.createElement('div');\n" +
                "      div.className = 'result-msg ' + type + ' fade-in';\n" +
                "      div.innerHTML = '<p>' + escapeText(message) + '</p>';\n" +
                "      area.insertBefore(div, area.firstChild);\n" +
                "      setTimeout(function() { div.remove(); }, 5000);\n" +
                "    }\n" +
                "\n" +
                "    /* ===== API CALLS ===== */\n" +
                "\n" +
                "    function loadScenarios() {\n" +
                "      console.log('[Game] Loading scenarios...');\n" +
                "      fetch('/api/v1/earth2026/scenarios?level=1', {\n" +
                "        headers: token ? { 'Authorization': 'Bearer ' + token } : {}\n" +
                "      })\n" +
                "      .then(function(r) {\n" +
                "        if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
                "        return r.json();\n" +
                "      })\n" +
                "      .then(function(data) {\n" +
                "        scenarios = data || [];\n" +
                "        console.log('[Game] Loaded', scenarios.length, 'scenarios');\n" +
                "        renderScene('intro');\n" +
                "      })\n" +
                "      .catch(function(err) {\n" +
                "        console.error('[Game] Failed to load scenarios:', err);\n" +
                "        document.getElementById('gameArea').innerHTML = '<p class=\"scene-narrative\">Failed to load missions. Please try again.</p>';\n" +
                "      });\n" +
                "    }\n" +
                "\n" +
                "    function startScenario(scenarioId) {\n" +
                "      console.log('[Game] Starting scenario:', scenarioId);\n" +
                "      var btn = event.target;\n" +
                "      btn.disabled = true;\n" +
                "      btn.textContent = '⏳ Starting...';\n" +
                "\n" +
                "      fetch('/api/v1/earth2026/scenarios/' + scenarioId + '/start', {\n" +
                "        method: 'POST',\n" +
                "        headers: {\n" +
                "          'Content-Type': 'application/json',\n" +
                "          'Authorization': token ? 'Bearer ' + token : ''\n" +
                "        }\n" +
                "      })\n" +
                "      .then(function(r) {\n" +
                "        if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
                "        return r.json();\n" +
                "      })\n" +
                "      .then(function(data) {\n" +
                "        console.log('[Game] Scenario started:', data);\n" +
                "        var scenarioName = '';\n" +
                "        for (var i = 0; i < scenarios.length; i++) {\n" +
                "          if (String(scenarios[i].id) === String(scenarioId)) {\n" +
                "            scenarioName = scenarios[i].name;\n" +
                "            break;\n" +
                "          }\n" +
                "        }\n" +
                "        showResult('success', 'Mission started: ' + (scenarioName || 'Unknown'));\n" +
                "        // Sync game state to server\n" +
                "        syncGameState({ scenarioId: scenarioId, status: 'started' });\n" +
                "      })\n" +
                "      .catch(function(err) {\n" +
                "        console.error('[Game] Failed to start scenario:', err);\n" +
                "        showResult('error', 'Failed to start mission: ' + err.message);\n" +
                "        btn.disabled = false;\n" +
                "        btn.textContent = '▶ Start Mission';\n" +
                "      });\n" +
                "    }\n" +
                "\n" +
                "    function syncGameState(state) {\n" +
                "      if (!token) return;\n" +
                "      console.log('[Game] Syncing state:', state);\n" +
                "      var payload = {\n" +
                "        type: 'STATE_UPDATE',\n" +
                "        playerId: localStorage.getItem('selectedCharacterId') || 'unknown',\n" +
                "        sessionId: 'earth2026-' + Date.now(),\n" +
                "        timestamp: Date.now(),\n" +
                "        state: state,\n" +
                "        version: (parseInt(localStorage.getItem('gameStateVersion') || '0')) + 1\n" +
                "      };\n" +
                "      fetch('/api/v1/game-sync/state', {\n" +
                "        method: 'POST',\n" +
                "        headers: {\n" +
                "          'Content-Type': 'application/json',\n" +
                "          'Authorization': 'Bearer ' + token\n" +
                "        },\n" +
                "        body: JSON.stringify(payload)\n" +
                "      })\n" +
                "      .then(function(r) { return r.json(); })\n" +
                "      .then(function(data) {\n" +
                "        if (data.version) localStorage.setItem('gameStateVersion', String(data.version));\n" +
                "        console.log('[Game] State synced, version:', data.version);\n" +
                "      })\n" +
                "      .catch(function(err) { console.error('[Game] Sync failed:', err); });\n" +
                "    }\n" +
                "\n" +
                "    function escapeText(t) {\n" +
                "      if (!t) return '';\n" +
                "      return String(t)\n" +
                "        .replace('&', '&amp;')\n" +
                "        .replace('<', '&lt;')\n" +
                "        .replace('>', '&gt;')\n" +
                "        .replace('\"', '&quot;');\n" +
                "    }\n" +
                "\n" +
                "    /* ===== INIT ===== */\n" +
                "\n" +
                "    console.log('[Game] Initializing Earth 2026 game engine...');\n" +
                "    loadScenarios();\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(html);
    }

    /**
     * Traveller game entry page — launched from the character panel.
     */
    @GetMapping({"/game/traveller", "/game/traveller/"})
    public ResponseEntity<String> travellerGamePage(HttpServletRequest request) {
        String characterId = request.getParameter("characterId");
        String charName = characterId != null ? "Character " + escapeHtml(characterId) : "Spacer";

        String html = "<!DOCTYPE html>\n" +
                "<html lang='en'>\n" +
                "<head>\n" +
                "  <meta charset='UTF-8'>\n" +
                "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "  <title>Traveller - " + charName + "</title>\n" +
                "  <style>\n" +
                "    * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "    body { background: #0a0a1a; color: #e0e0e0; font-family: 'Segoe UI', system-ui, sans-serif; min-height: 100vh; }\n" +
                "    .header { background: #131926; padding: 16px 24px; border-bottom: 1px solid #1e2840; display: flex; justify-content: space-between; align-items: center; }\n" +
                "    .header h1 { color: #00d4ff; font-size: 1.2em; }\n" +
                "    .header a { color: #9aa0a6; text-decoration: none; font-size: 0.85em; }\n" +
                "    .header a:hover { color: #00d4ff; }\n" +
                "    .container { max-width: 960px; margin: 32px auto; padding: 0 24px; }\n" +
                "    .welcome { background: #131926; padding: 24px; border-radius: 10px; border: 1px solid #1e2840; margin-bottom: 24px; }\n" +
                "    .welcome h2 { color: #00d4ff; margin-bottom: 8px; }\n" +
                "    .welcome p { color: #9aa0a6; line-height: 1.6; }\n" +
                "    .game-area { background: #131926; padding: 24px; border-radius: 10px; border: 1px solid #1e2840; min-height: 400px; }\n" +
                "    .game-area h3 { color: #00d4ff; margin-bottom: 16px; }\n" +
                "    .game-area p { color: #9aa0a6; line-height: 1.6; margin-bottom: 12px; }\n" +
                "    .scene { padding: 16px 0; border-bottom: 1px solid #1e2840; margin-bottom: 16px; }\n" +
                "    .scene:last-child { border-bottom: none; }\n" +
                "    .scene-narrative { color: #9aa0a6; line-height: 1.8; margin-bottom: 16px; font-size: 0.95em; }\n" +
                "    .scene-choices { display: flex; flex-direction: column; gap: 8px; margin-top: 12px; }\n" +
                "    .choice-btn { display: inline-block; background: #1a2332; color: #00d4ff; padding: 12px 20px; border-radius: 6px; border: 1px solid #1e2840; font-size: 0.9em; cursor: pointer; text-align: left; transition: all 0.2s; }\n" +
                "    .choice-btn:hover { background: #243044; border-color: #00d4ff; }\n" +
                "    .action-btn { display: inline-block; background: #00d4ff; color: #0a0a1a; padding: 10px 20px; border-radius: 6px; text-decoration: none; font-size: 0.9em; font-weight: 600; cursor: pointer; border: none; margin-top: 8px; }\n" +
                "    .action-btn:hover { background: #00b8d4; }\n" +
                "    .action-btn:disabled { background: #333; color: #666; cursor: not-allowed; }\n" +
                "    .mission-card { background: #1a2332; padding: 16px; border-radius: 8px; border: 1px solid #1e2840; margin-bottom: 12px; }\n" +
                "    .mission-card h4 { color: #00d4ff; margin-bottom: 8px; font-size: 1em; }\n" +
                "    .mission-card p { color: #9aa0a6; line-height: 1.5; margin-bottom: 8px; font-size: 0.9em; }\n" +
                "    .mission-meta { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 12px; }\n" +
                "    .meta-tag { background: #0a0a1a; color: #9aa0a6; padding: 4px 10px; border-radius: 4px; font-size: 0.75em; text-transform: uppercase; }\n" +
                "    .meta-tag.difficulty { color: #ff9800; }\n" +
                "    .meta-tag.type { color: #4caf50; }\n" +
                "    .meta-tag.xp { color: #e040fb; }\n" +
                "    .hidden { display: none; }\n" +
                "    .fade-in { animation: fadeIn 0.5s ease-in; }\n" +
                "    @keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }\n" +
                "    .status-bar { margin-top: 24px; padding: 16px 20px; background: #131926; border-radius: 10px; border: 1px solid #1e2840; }\n" +
                "    .status-bar h4 { color: #9aa0a6; font-size: 0.85em; margin-bottom: 8px; text-transform: uppercase; }\n" +
                "    .status { display: flex; gap: 24px; flex-wrap: wrap; }\n" +
                "    .status-item { display: flex; flex-direction: column; gap: 4px; }\n" +
                "    .label { color: #9aa0a6; font-size: 0.75em; text-transform: uppercase; }\n" +
                "    .value { color: #e0e0e0; font-size: 0.9em; }\n" +
                "    .online { color: #4caf50; }\n" +
                "    .loading { color: #9aa0a6; text-align: center; padding: 40px; }\n" +
                "    .loading::after { content: '...'; animation: dots 1.5s infinite; }\n" +
                "    @keyframes dots { 0%, 20% { content: '.'; } 40% { content: '..'; } 60%, 100% { content: '...'; } }\n" +
                "    .result-msg { background: #1a2332; padding: 16px; border-radius: 8px; border-left: 3px solid #00d4ff; margin-top: 12px; }\n" +
                "    .result-msg.success { border-left-color: #4caf50; }\n" +
                "    .result-msg.error { border-left-color: #f44336; }\n" +
                "    .char-stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(120px, 1fr)); gap: 12px; margin: 16px 0; }\n" +
                "    .stat-box { background: #0a0a1a; padding: 12px; border-radius: 6px; text-align: center; }\n" +
                "    .stat-box .stat-label { color: #9aa0a6; font-size: 0.7em; text-transform: uppercase; margin-bottom: 4px; }\n" +
                "    .stat-box .stat-value { color: #00d4ff; font-size: 1.4em; font-weight: 700; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class='header'>\n" +
                "    <h1>🚀 Traveller</h1>\n" +
                "    <a href='/corporation/'>← Back to Dashboard</a>\n" +
                "  </div>\n" +
                "  <div class='container'>\n" +
                "    <div class='welcome'>\n" +
                "      <h2>Welcome to the Imperium, " + charName + "!</h2>\n" +
                "      <p>Your starship awaits. Explore the hex crawl, trade between worlds, and face the dangers of the void.</p>\n" +
                "      <div class='char-stats' id='charStats'>\n" +
                "        <div class='stat-box'><div class='stat-label'>Character</div><div class='stat-value' id='charId'>" + charName + "</div></div>\n" +
                "        <div class='stat-box'><div class='stat-label'>Ship</div><div class='stat-value' id='shipName'>—</div></div>\n" +
                "        <div class='stat-box'><div class='stat-label'>Credits</div><div class='stat-value' id='credits'>—</div></div>\n" +
                "        <div class='stat-box'><div class='stat-label'>System</div><div class='stat-value' id='currentSystem'>—</div></div>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "    <div class='game-area' id='gameArea'>\n" +
                "      <div class='loading' id='loadingMsg'>Loading your adventure</div>\n" +
                "    </div>\n" +
                "    <div class='status-bar'>\n" +
                "      <h4>Game Node Status</h4>\n" +
                "      <div class='status'>\n" +
                "        <div class='status-item'><span class='label'>Connection</span><span class='value online'>● Connected</span></div>\n" +
                "        <div class='status-item'><span class='label'>Game Engine</span><span class='value'>Running</span></div>\n" +
                "        <div class='status-item'><span class='label'>AI Model</span><span class='value'>traveller-ai v1</span></div>\n" +
                "        <div class='status-item'><span class='label'>Character</span><span class='value'>" + charName + "</span></div>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "  <script>\n" +
                "    var token = localStorage.getItem('accessToken');\n" +
                "    var missions = [];\n" +
                "    var currentScene = 'intro';\n" +
                "\n" +
                "    /* ===== SCENE DEFINITIONS ===== */\n" +
                "\n" +
                "    var scenes = {\n" +
                "      intro: {\n" +
                "        title: '📍 Port Sol - Space Station',\n" +
                "        narrative: 'You dock at the bustling space station orbiting Sol. The airlock hisses open and you step into the crowded concourse. Merchants hawk their wares, mercenaries gather near the bar, and a recruiter from the Imperial Navy approaches you.',\n" +
                "        dialogue: '\"We need spacers for a mission beyond the rim. The pay is good, the risk is higher.\"',\n" +
                "        choices: [\n" +
                "          { text: '⚔️ Accept the Mission', next: 'missions' },\n" +
                "          { text: '🔍 Explore the Station', next: 'explore' },\n" +
                "          { text: '💰 Check the Trade Market', next: 'trade' }\n" +
                "        ]\n" +
                "      },\n" +
                "      explore: {\n" +
                "        title: '📍 Space Station Concourse',\n" +
                "        narrative: 'You wander through the station. The bar is full of rough spacers sharing tales of alien encounters and pirate raids. The shipyard bay offers a view of your vessel, undergoing minor repairs. A holographic map shows nearby star systems and their trade routes.',\n" +
                "        dialogue: 'The station master nods as you pass. \"Careful out there, spacer. The rim is no place for the faint of heart.\"',\n" +
                "        choices: [\n" +
                "          { text: '⚔️ See the Recruiter', next: 'missions' },\n" +
                "          { text: '🚀 Check Your Ship', next: 'ship' }\n" +
                "        ]\n" +
                "      },\n" +
                "      trade: {\n" +
                "        title: '📍 Trade Market',\n" +
                "        narrative: 'The trade market buzzes with activity. Commodities prices fluctuate on holographic displays. You could buy supplies, sell cargo, or invest in a trade route. A shady dealer offers you contraband at a discount.',\n" +
                "        dialogue: '\"Off the books, of course. No questions asked.\"',\n" +
                "        choices: [\n" +
                "          { text: '⚔️ See the Recruiter', next: 'missions' },\n" +
                "          { text: '🔍 Explore the Station', next: 'explore' }\n" +
                "        ]\n" +
                "      },\n" +
                "      ship: {\n" +
                "        title: '📍 Shipyard Bay',\n" +
                "        narrative: 'Your ship sits in the bay, hull gleaming under the station lights. The crew is running diagnostics on the drive core. The navigation computer shows nearby systems and jump coordinates.',\n" +
                "        dialogue: 'The chief engineer reports: \"She\\'s ready to jump, captain. Where to?\"',\n" +
                "        choices: [\n" +
                "          { text: '⚔️ See the Recruiter', next: 'missions' },\n" +
                "          { text: '🔍 Explore the Station', next: 'explore' }\n" +
                "        ]\n" +
                "      },\n" +
                "      missions: {\n" +
                "        title: '📋 Available Missions',\n" +
                "        narrative: 'The recruiter lays out the available missions. Each one carries risk and reward. Choose wisely, spacer.',\n" +
                "        choices: []\n" +
                "      }\n" +
                "    };\n" +
                "\n" +
                "    /* ===== RENDERING ===== */\n" +
                "\n" +
                "    function renderScene(sceneId) {\n" +
                "      currentScene = sceneId;\n" +
                "      var scene = scenes[sceneId];\n" +
                "      if (!scene) return;\n" +
                "\n" +
                "      var area = document.getElementById('gameArea');\n" +
                "      var html = '<div class=\"scene fade-in\">';\n" +
                "      html += '<h3>' + escapeText(scene.title) + '</h3>';\n" +
                "      html += '<p class=\"scene-narrative\">' + escapeText(scene.narrative) + '</p>';\n" +
                "      if (scene.dialogue) {\n" +
                "        html += '<p style=\"color:#00d4ff;font-style:italic;margin-bottom:12px;\">' + escapeText(scene.dialogue) + '</p>';\n" +
                "      }\n" +
                "\n" +
                "      if (sceneId === 'missions') {\n" +
                "        html += '<div id=\"missionList\">';\n" +
                "        if (missions.length === 0) {\n" +
                "          html += '<p class=\"scene-narrative\">Loading missions...</p>';\n" +
                "        } else {\n" +
                "          missions.forEach(function(m) {\n" +
                "            html += '<div class=\"mission-card\">';\n" +
                "            html += '<h4>' + escapeText(m.name || 'Unknown Mission') + '</h4>';\n" +
                "            if (m.description) html += '<p>' + escapeText(m.description) + '</p>';\n" +
                "            html += '<div class=\"mission-meta\">';\n" +
                "            if (m.type) html += '<span class=\"meta-tag type\">' + escapeText(m.type) + '</span>';\n" +
                "            if (m.difficulty) html += '<span class=\"meta-tag difficulty\">' + escapeText(m.difficulty) + '</span>';\n" +
                "            if (m.rewards) html += '<span class=\"meta-tag xp\">' + escapeText(m.rewards) + '</span>';\n" +
                "            html += '</div>';\n" +
                "            html += '<button class=\"action-btn\" onclick=\"startMission(\\'' + escapeText(String(m.id || m.name)) + '\\')\">▶ Start Mission</button>';\n" +
                "            html += '</div>';\n" +
                "          });\n" +
                "        }\n" +
                "        html += '</div>';\n" +
                "        html += '<div class=\"scene-choices\" style=\"margin-top:16px\">';\n" +
                "        html += '<button class=\"choice-btn\" onclick=\"renderScene(\\'intro\\')\">↩ Return to Station</button>';\n" +
                "        html += '</div>';\n" +
                "      } else {\n" +
                "        if (scene.choices && scene.choices.length > 0) {\n" +
                "          html += '<div class=\"scene-choices\">';\n" +
                "          scene.choices.forEach(function(c) {\n" +
                "            html += '<button class=\"choice-btn\" onclick=\"renderScene(\\'' + c.next + '\\')\">' + escapeText(c.text) + '</button>';\n" +
                "          });\n" +
                "          html += '</div>';\n" +
                "        }\n" +
                "      }\n" +
                "      html += '</div>';\n" +
                "      area.innerHTML = html;\n" +
                "    }\n" +
                "\n" +
                "    function showResult(type, message) {\n" +
                "      var area = document.getElementById('gameArea');\n" +
                "      var div = document.createElement('div');\n" +
                "      div.className = 'result-msg ' + type + ' fade-in';\n" +
                "      div.innerHTML = '<p>' + escapeText(message) + '</p>';\n" +
                "      area.insertBefore(div, area.firstChild);\n" +
                "      setTimeout(function() { div.remove(); }, 5000);\n" +
                "    }\n" +
                "\n" +
                "    /* ===== API CALLS ===== */\n" +
                "\n" +
                "    function loadMissions() {\n" +
                "      console.log('[Traveller] Loading missions...');\n" +
                "      fetch('/api/v1/missions', {\n" +
                "        headers: token ? { 'Authorization': 'Bearer ' + token } : {}\n" +
                "      })\n" +
                "      .then(function(r) {\n" +
                "        if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
                "        return r.json();\n" +
                "      })\n" +
                "      .then(function(data) {\n" +
                "        missions = data || [];\n" +
                "        console.log('[Traveller] Loaded', missions.length, 'missions');\n" +
                "        renderScene('intro');\n" +
                "      })\n" +
                "      .catch(function(err) {\n" +
                "        console.error('[Traveller] Failed to load missions:', err);\n" +
                "        document.getElementById('gameArea').innerHTML = '<p class=\"scene-narrative\">Failed to load missions. Please try again.</p>';\n" +
                "      });\n" +
                "    }\n" +
                "\n" +
                "    function loadCharacterInfo() {\n" +
                "      if (!'" + characterId + "') return;\n" +
                "      console.log('[Traveller] Loading character info for:', '" + characterId + "');\n" +
                "      fetch('/api/v1/characters', {\n" +
                "        headers: token ? { 'Authorization': 'Bearer ' + token } : {}\n" +
                "      })\n" +
                "      .then(function(r) { return r.json(); })\n" +
                "      .then(function(data) {\n" +
                "        var chars = data || [];\n" +
                "        var char = chars.find(function(c) { return String(c.id) === String('" + characterId + "'); });\n" +
                "        if (char) {\n" +
                "          document.getElementById('charId').textContent = char.name || char.id;\n" +
                "          if (char.ship) document.getElementById('shipName').textContent = char.ship;\n" +
                "          if (char.credits) document.getElementById('credits').textContent = char.credits;\n" +
                "          if (char.currentSystem) document.getElementById('currentSystem').textContent = char.currentSystem;\n" +
                "        }\n" +
                "      })\n" +
                "      .catch(function(err) { console.error('[Traveller] Failed to load character:', err); });\n" +
                "    }\n" +
                "\n" +
                "    function startMission(missionId) {\n" +
                "      console.log('[Traveller] Starting mission:', missionId);\n" +
                "      var btn = event.target;\n" +
                "      btn.disabled = true;\n" +
                "      btn.textContent = '⏳ Starting...';\n" +
                "\n" +
                "      var payload = {\n" +
                "        type: 'STATE_UPDATE',\n" +
                "        playerId: localStorage.getItem('selectedCharacterId') || '" + characterId + "',\n" +
                "        sessionId: 'traveller-' + Date.now(),\n" +
                "        timestamp: Date.now(),\n" +
                "        state: { missionId: missionId, status: 'started' },\n" +
                "        version: (parseInt(localStorage.getItem('gameStateVersion') || '0')) + 1\n" +
                "      };\n" +
                "\n" +
                "      fetch('/api/v1/game-sync/state', {\n" +
                "        method: 'POST',\n" +
                "        headers: {\n" +
                "          'Content-Type': 'application/json',\n" +
                "          'Authorization': token ? 'Bearer ' + token : ''\n" +
                "        },\n" +
                "        body: JSON.stringify(payload)\n" +
                "      })\n" +
                "      .then(function(r) { return r.json(); })\n" +
                "      .then(function(data) {\n" +
                "        console.log('[Traveller] Mission started:', data);\n" +
                "        if (data.version) localStorage.setItem('gameStateVersion', String(data.version));\n" +
                "        showResult('success', 'Mission started: ' + escapeText(missionId));\n" +
                "      })\n" +
                "      .catch(function(err) {\n" +
                "        console.error('[Traveller] Failed to start mission:', err);\n" +
                "        showResult('error', 'Failed to start mission: ' + err.message);\n" +
                "        btn.disabled = false;\n" +
                "        btn.textContent = '▶ Start Mission';\n" +
                "      });\n" +
                "    }\n" +
                "\n" +
                "    function escapeText(t) {\n" +
                "      if (!t) return '';\n" +
                "      return String(t)\n" +
                "        .replace('&', '&amp;')\n" +
                "        .replace('<', '&lt;')\n" +
                "        .replace('>', '&gt;')\n" +
                "        .replace('\"', '&quot;');\n" +
                "    }\n" +
                "\n" +
                "    /* ===== INIT ===== */\n" +
                "\n" +
                "    console.log('[Traveller] Initializing game engine...');\n" +
                "    loadCharacterInfo();\n" +
                "    loadMissions();\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(html);
    }

    /**
     * Earth 2026 RPG FPS page — first-person shooter game mode.
     * Launches the FPS client with Earth 2026 themed missions and maps.
     */
    @GetMapping(value = "/earth2026-fps/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> earth2026FpsPage() {
        String html = "<!DOCTYPE html>\n" +
                "<html lang='en'>\n" +
                "<head>\n" +
                "  <meta charset='UTF-8'>\n" +
                "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "  <title>Earth 2026 - RPG FPS</title>\n" +
                "  <style>\n" +
                "    * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "    body { background: #0a0a1a; color: #e0e0e0; font-family: 'Segoe UI', system-ui, sans-serif; overflow: hidden; }\n" +
                "    .header { background: #131926; padding: 12px 24px; border-bottom: 1px solid #1e2840; display: flex; justify-content: space-between; align-items: center; z-index: 100; position: relative; }\n" +
                "    .header h1 { color: #00d4ff; font-size: 1.1em; }\n" +
                "    .header a { color: #9aa0a6; text-decoration: none; font-size: 0.85em; }\n" +
                "    .header a:hover { color: #00d4ff; }\n" +
                "    #gameCanvas { display: block; width: 100vw; height: 100vh; }\n" +
                "    #hud { position: fixed; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none; z-index: 10; display: none; }\n" +
                "    #crosshair { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 20px; height: 20px; }\n" +
                "    #crosshair::before, #crosshair::after { content: ''; position: absolute; background: rgba(255,255,255,0.8); }\n" +
                "    #crosshair::before { top: 50%; left: 0; width: 100%; height: 2px; transform: translateY(-50%); }\n" +
                "    #crosshair::after { top: 0; left: 50%; width: 2px; height: 100%; transform: translateX(-50%); }\n" +
                "    #healthBar { position: absolute; bottom: 30px; left: 30px; width: 250px; height: 20px; background: rgba(0,0,0,0.6); border: 2px solid rgba(255,255,255,0.3); border-radius: 4px; overflow: hidden; }\n" +
                "    #healthFill { height: 100%; width: 100%; background: linear-gradient(90deg, #ff4444, #44ff44); transition: width 0.3s ease; }\n" +
                "    #healthText { position: absolute; bottom: 55px; left: 30px; color: #fff; font-size: 14px; text-shadow: 1px 1px 2px #000; }\n" +
                "    #ammoCounter { position: absolute; bottom: 30px; right: 30px; color: #fff; font-size: 28px; font-weight: bold; text-shadow: 2px 2px 4px #000; }\n" +
                "    #weaponName { position: absolute; bottom: 65px; right: 30px; color: #aaa; font-size: 14px; text-shadow: 1px 1px 2px #000; }\n" +
                "    #scoreDisplay { position: absolute; top: 20px; left: 50%; transform: translateX(-50%); color: #fff; font-size: 18px; text-align: center; text-shadow: 2px 2px 4px #000; }\n" +
                "    #killFeed { position: absolute; top: 20px; right: 20px; width: 250px; }\n" +
                "    .kill-feed-item { background: rgba(0,0,0,0.6); color: #fff; padding: 5px 10px; margin-bottom: 4px; border-radius: 3px; font-size: 12px; animation: fadeOut 5s forwards; }\n" +
                "    @keyframes fadeOut { 0%, 70% { opacity: 1; } 100% { opacity: 0; } }\n" +
                "    #damageOverlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: radial-gradient(ellipse at center, transparent 50%, rgba(255,0,0,0.4) 100%); opacity: 0; transition: opacity 0.3s; pointer-events: none; z-index: 5; }\n" +
                "    #minimap { position: absolute; top: 20px; left: 20px; width: 150px; height: 150px; background: rgba(0,0,0,0.6); border: 2px solid rgba(255,255,255,0.3); border-radius: 50%; overflow: hidden; }\n" +
                "    #minimapCanvas { width: 100%; height: 100%; }\n" +
                "    #reloadIndicator { position: absolute; top: 60%; left: 50%; transform: translateX(-50%); color: #ffaa00; font-size: 16px; text-shadow: 1px 1px 2px #000; display: none; }\n" +
                "    #lobbyScreen { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); display: flex; flex-direction: column; align-items: center; justify-content: center; z-index: 100; }\n" +
                "    #lobbyScreen h1 { color: #fff; font-size: 48px; margin-bottom: 10px; text-shadow: 0 0 20px rgba(100,200,255,0.5); }\n" +
                "    #lobbyScreen p { color: #888; margin-bottom: 30px; }\n" +
                "    .lobby-btn { background: linear-gradient(135deg, #0f3460, #16213e); color: #fff; border: 1px solid rgba(100,200,255,0.3); padding: 12px 30px; margin: 5px; font-size: 16px; border-radius: 5px; cursor: pointer; pointer-events: auto; transition: all 0.3s; }\n" +
                "    .lobby-btn:hover { background: linear-gradient(135deg, #16213e, #0f3460); border-color: rgba(100,200,255,0.6); box-shadow: 0 0 15px rgba(100,200,255,0.3); }\n" +
                "    .lobby-btn:disabled { opacity: 0.5; cursor: not-allowed; }\n" +
                "    #lobbyStatus { color: #888; margin-top: 20px; font-size: 14px; }\n" +
                "    #matchList { max-height: 200px; overflow-y: auto; margin: 15px 0; width: 400px; max-width: 90vw; }\n" +
                "    .match-item { background: rgba(255,255,255,0.05); padding: 10px 15px; margin: 5px 0; border-radius: 5px; cursor: pointer; color: #ccc; transition: background 0.2s; }\n" +
                "    .match-item:hover { background: rgba(255,255,255,0.1); }\n" +
                "    #deathScreen { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(139,0,0,0.7); display: none; flex-direction: column; align-items: center; justify-content: center; z-index: 50; }\n" +
                "    #deathScreen h2 { color: #fff; font-size: 36px; margin-bottom: 20px; }\n" +
                "    #respawnTimer { color: #ffaa00; font-size: 24px; }\n" +
                "    #playerCount { position: absolute; bottom: 30px; left: 50%; transform: translateX(-50%); color: #aaa; font-size: 14px; text-shadow: 1px 1px 2px #000; }\n" +
                "    #missionBriefing { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); background: rgba(19,25,38,0.95); padding: 30px; border-radius: 10px; border: 1px solid #00d4ff; max-width: 500px; text-align: center; display: none; z-index: 200; pointer-events: auto; }\n" +
                "    #missionBriefing h2 { color: #00d4ff; margin-bottom: 15px; }\n" +
                "    #missionBriefing p { color: #9aa0a6; margin-bottom: 20px; line-height: 1.6; }\n" +
                "    .mission-btn { background: #00d4ff; color: #0a0a1a; border: none; padding: 10px 25px; border-radius: 5px; font-size: 16px; cursor: pointer; pointer-events: auto; }\n" +
                "    .mission-btn:hover { background: #00b8d4; }\n" +
                "    #sanityBar { position: absolute; bottom: 80px; left: 30px; width: 250px; height: 12px; background: rgba(0,0,0,0.6); border: 2px solid rgba(138,43,226,0.5); border-radius: 4px; overflow: hidden; }\n" +
                "    #sanityFill { height: 100%; width: 100%; background: linear-gradient(90deg, #8a2be2, #9370db); transition: width 0.3s ease; }\n" +
                "    #sanityText { position: absolute; bottom: 95px; left: 30px; color: #9370db; font-size: 12px; text-shadow: 1px 1px 2px #000; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <canvas id='gameCanvas'></canvas>\n" +
                "  <div id='damageOverlay'></div>\n" +
                "  <div id='hud'>\n" +
                "    <div id='crosshair'></div>\n" +
                "    <div id='sanityText'>Sanity: 100</div>\n" +
                "    <div id='sanityBar'><div id='sanityFill'></div></div>\n" +
                "    <div id='healthText'>Health: 100</div>\n" +
                "    <div id='healthBar'><div id='healthFill'></div></div>\n" +
                "    <div id='weaponName'>M4 Assault Rifle</div>\n" +
                "    <div id='ammoCounter'>30 / 120</div>\n" +
                "    <div id='scoreDisplay'><span id='scoreText'>Score: 0</span> | <span id='matchTimer'>05:00</span></div>\n" +
                "    <div id='killFeed'></div>\n" +
                "    <div id='minimap'><canvas id='minimapCanvas' width='150' height='150'></canvas></div>\n" +
                "    <div id='reloadIndicator'>RELOADING...</div>\n" +
                "    <div id='playerCount'>Players: 1</div>\n" +
                "  </div>\n" +
                "  <div id='deathScreen'><h2>YOU DIED</h2><div id='respawnTimer'>Respawning in 5...</div></div>\n" +
                "  <div id='missionBriefing'><h2 id='missionTitle'>Mission Briefing</h2><p id='missionDesc'></p><button class='mission-btn' onclick='startMission()'>Begin Mission</button></div>\n" +
                "  <div id='lobbyScreen'>\n" +
                "    <h1>🌍 Earth 2026</h1>\n" +
                "    <p>RPG FPS - First Person Shooter</p>\n" +
                "    <div>\n" +
                "      <button class='lobby-btn' id='btnCreateMatch'>New Mission</button>\n" +
                "      <button class='lobby-btn' id='btnJoinMatch'>Quick Play</button>\n" +
                "    </div>\n" +
                "    <div id='matchList'></div>\n" +
                "    <div id='lobbyStatus'>Loading missions...</div>\n" +
                "  </div>\n" +
                "  <script src='https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js'></script>\n" +
                "  <script>\n" +
                "    // ===== Earth 2026 RPG FPS Game =====\n" +
                "    \n" +
                "    class Earth2026FpsGame {\n" +
                "      constructor() {\n" +
                "        this.canvas = document.getElementById('gameCanvas');\n" +
                "        this.scene = null;\n" +
                "        this.camera = null;\n" +
                "        this.renderer = null;\n" +
                "        this.clock = new THREE.Clock();\n" +
                "        this.isInMatch = false;\n" +
                "        this.isAlive = true;\n" +
                "        this.health = 100;\n" +
                "        this.sanity = 100;\n" +
                "        this.score = 0;\n" +
                "        this.kills = 0;\n" +
                "        this.deaths = 0;\n" +
                "        this.currentWeapon = null;\n" +
                "        this.weapons = [];\n" +
                "        this.enemies = [];\n" +
                "        this.projectiles = [];\n" +
                "        this.map = null;\n" +
                "        this.missions = [];\n" +
                "        this.currentMission = null;\n" +
                "        this.playerPosition = new THREE.Vector3(0, 1.7, 0);\n" +
                "        this.playerRotation = { yaw: 0, pitch: 0 };\n" +
                "        this.keys = {};\n" +
                "        this.mouse = { x: 0, y: 0, down: false };\n" +
                "        this.init();\n" +
                "      }\n" +
                "      \n" +
                "      init() {\n" +
                "        // Three.js setup\n" +
                "        this.scene = new THREE.Scene();\n" +
                "        this.scene.background = new THREE.Color(0x1a1a2e);\n" +
                "        this.scene.fog = new THREE.Fog(0x1a1a2e, 30, 100);\n" +
                "        \n" +
                "        const width = window.innerWidth;\n" +
                "        const height = window.innerHeight;\n" +
                "        this.camera = new THREE.PerspectiveCamera(75, width / height, 0.1, 1000);\n" +
                "        this.camera.position.copy(this.playerPosition);\n" +
                "        \n" +
                "        this.renderer = new THREE.WebGLRenderer({ canvas: this.canvas, antialias: true });\n" +
                "        this.renderer.setSize(width, height);\n" +
                "        this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));\n" +
                "        this.renderer.shadowMap.enabled = true;\n" +
                "        \n" +
                "        // Lighting\n" +
                "        const ambientLight = new THREE.AmbientLight(0x404040, 0.8);\n" +
                "        this.scene.add(ambientLight);\n" +
                "        \n" +
                "        const dirLight = new THREE.DirectionalLight(0xffffff, 0.6);\n" +
                "        dirLight.position.set(50, 100, 50);\n" +
                "        dirLight.castShadow = true;\n" +
                "        this.scene.add(dirLight);\n" +
                "        \n" +
                "        // Ground plane\n" +
                "        const groundGeo = new THREE.PlaneGeometry(200, 200);\n" +
                "        const groundMat = new THREE.MeshStandardMaterial({ color: 0x2a2a3a });\n" +
                "        const ground = new THREE.Mesh(groundGeo, groundMat);\n" +
                "        ground.rotation.x = -Math.PI / 2;\n" +
                "        ground.receiveShadow = true;\n" +
                "        this.scene.add(ground);\n" +
                "        \n" +
                "        // Load weapons and missions\n" +
                "        this.loadWeapons();\n" +
                "        this.loadMissions();\n" +
                "        \n" +
                "        // Event listeners\n" +
                "        document.addEventListener('keydown', (e) => this.keys[e.code] = true);\n" +
                "        document.addEventListener('keyup', (e) => this.keys[e.code] = false);\n" +
                "        document.addEventListener('mousemove', (e) => this.onMouseMove(e));\n" +
                "        document.addEventListener('mousedown', (e) => { this.mouse.down = true; this.shoot(); });\n" +
                "        document.addEventListener('mouseup', () => this.mouse.down = false);\n" +
                "        document.addEventListener('pointerlockchange', () => this.onPointerLockChange());\n" +
                "        window.addEventListener('resize', () => this.onResize());\n" +
                "        \n" +
                "        // Lobby buttons\n" +
                "        document.getElementById('btnCreateMatch').addEventListener('click', () => this.createMatch());\n" +
                "        document.getElementById('btnJoinMatch').addEventListener('click', () => this.quickPlay());\n" +
                "        \n" +
                "        // Start animation loop\n" +
                "        this.animate();\n" +
                "      }\n" +
                "      \n" +
                "      loadWeapons() {\n" +
                "        fetch('/api/v1/fps/weapons')\n" +
                "          .then(r => r.json())\n" +
                "          .then(weapons => {\n" +
                "            this.weapons = weapons;\n" +
                "            if (weapons.length > 0) {\n" +
                "              this.currentWeapon = weapons[0];\n" +
                "              this.updateWeaponDisplay();\n" +
                "            }\n" +
                "          })\n" +
                "          .catch(e => console.warn('[Earth2026 FPS] Failed to load weapons:', e));\n" +
                "      }\n" +
                "      \n" +
                "      loadMissions() {\n" +
                "        // Load Earth 2026 scenarios as missions\n" +
                "        fetch('/api/v1/earth2026/scenarios?level=1')\n" +
                "          .then(r => r.json())\n" +
                "          .then(scenarios => {\n" +
                "            this.missions = scenarios || [];\n" +
                "            document.getElementById('lobbyStatus').textContent = this.missions.length + ' missions available';\n" +
                "            this.renderMissionList();\n" +
                "          })\n" +
                "          .catch(e => {\n" +
                "            console.warn('[Earth2026 FPS] Failed to load missions:', e);\n" +
                "            // Fallback missions\n" +
                "            this.missions = [\n" +
                "              { id: 'default_1', name: 'San Diego Streets', description: 'Clear the streets of corporate mercenaries.', map: 'San Diego Streets' },\n" +
                "              { id: 'default_2', name: 'Younger Industries HQ', description: 'Infiltrate the Younger Industries headquarters.', map: 'Younger Industries HQ' },\n" +
                "              { id: 'default_3', name: 'Area 51 Defense', description: 'Defend Area 51 against waves of enemies.', map: 'Area 51' }\n" +
                "            ];\n" +
                "            document.getElementById('lobbyStatus').textContent = this.missions.length + ' missions available';\n" +
                "            this.renderMissionList();\n" +
                "          });\n" +
                "      }\n" +
                "      \n" +
                "      renderMissionList() {\n" +
                "        const matchList = document.getElementById('matchList');\n" +
                "        let html = '';\n" +
                "        this.missions.forEach(mission => {\n" +
                "          html += '<div class=\"match-item\" onclick=\"game.selectMission(\\'' + mission.id + '\\')\">';\n" +
                "          html += '<strong>' + this.escapeText(mission.name || 'Unknown Mission') + '</strong><br>';\n" +
                "          html += '<small>' + this.escapeText(mission.description || '') + '</small>';\n" +
                "          html += '</div>';\n" +
                "        });\n" +
                "        matchList.innerHTML = html;\n" +
                "      }\n" +
                "      \n" +
                "      selectMission(missionId) {\n" +
                "        this.currentMission = this.missions.find(m => m.id === missionId) || this.missions[0];\n" +
                "        if (this.currentMission) {\n" +
                "          document.getElementById('missionTitle').textContent = this.currentMission.name;\n" +
                "          document.getElementById('missionDesc').textContent = this.currentMission.description;\n" +
                "          document.getElementById('missionBriefing').style.display = 'block';\n" +
                "          document.getElementById('lobbyScreen').style.display = 'none';\n" +
                "        }\n" +
                "      }\n" +
                "      \n" +
                "      createMatch() {\n" +
                "        // Create a new FPS match\n" +
                "        fetch('/api/v1/fps/matches?playerId=earth2026_player&playerName=Agent&mode=DEATHMATCH', { method: 'POST' })\n" +
                "          .then(r => r.json())\n" +
                "          .then(data => {\n" +
                "            console.log('[Earth2026 FPS] Match created:', data);\n" +
                "            this.startGameSession(data.roomId);\n" +
                "          })\n" +
                "          .catch(e => {\n" +
                "            console.warn('[Earth2026 FPS] Match creation failed, using local session:', e);\n" +
                "            this.startLocalSession();\n" +
                "          });\n" +
                "      }\n" +
                "      \n" +
                "      quickPlay() {\n" +
                "        // Quick play - start with a random mission\n" +
                "        if (this.missions.length > 0) {\n" +
                "          this.currentMission = this.missions[Math.floor(Math.random() * this.missions.length)];\n" +
                "          this.selectMission(this.currentMission.id);\n" +
                "        } else {\n" +
                "          this.startLocalSession();\n" +
                "        }\n" +
                "      }\n" +
                "      \n" +
                "      startGameSession(roomId) {\n" +
                "        this.isInMatch = true;\n" +
                "        document.getElementById('lobbyScreen').style.display = 'none';\n" +
                "        document.getElementById('hud').style.display = 'block';\n" +
                "        this.buildMap();\n" +
                "        this.spawnEnemies();\n" +
                "        this.canvas.requestPointerLock();\n" +
                "      }\n" +
                "      \n" +
                "      startLocalSession() {\n" +
                "        this.isInMatch = true;\n" +
                "        document.getElementById('lobbyScreen').style.display = 'none';\n" +
                "        document.getElementById('hud').style.display = 'block';\n" +
                "        this.buildMap();\n" +
                "        this.spawnEnemies();\n" +
                "        this.canvas.requestPointerLock();\n" +
                "      }\n" +
                "      \n" +
                "      buildMap() {\n" +
                "        // Clear existing map objects\n" +
                "        const toRemove = [];\n" +
                "        this.scene.children.forEach(child => {\n" +
                "          if (child.userData && child.userData.isMapObject) toRemove.push(child);\n" +
                "        });\n" +
                "        toRemove.forEach(obj => this.scene.remove(obj));\n" +
                "        \n" +
                "        // Build map based on current mission\n" +
                "        const mapName = (this.currentMission && this.currentMission.map) || 'San Diego Streets';\n" +
                "        console.log('[Earth2026 FPS] Building map:', mapName);\n" +
                "        \n" +
                "        // Add buildings/obstacles\n" +
                "        const buildingColors = [0x3a3a4a, 0x4a4a5a, 0x2a2a3a, 0x5a5a6a];\n" +
                "        for (let i = 0; i < 15; i++) {\n" +
                "          const width = 3 + Math.random() * 5;\n" +
                "          const height = 3 + Math.random() * 8;\n" +
                "          const depth = 3 + Math.random() * 5;\n" +
                "          const geo = new THREE.BoxGeometry(width, height, depth);\n" +
                "          const mat = new THREE.MeshStandardMaterial({ \n" +
                "            color: buildingColors[Math.floor(Math.random() * buildingColors.length)]\n" +
                "          });\n" +
                "          const building = new THREE.Mesh(geo, mat);\n" +
                "          building.position.set(\n" +
                "            (Math.random() - 0.5) * 80,\n" +
                "            height / 2,\n" +
                "            (Math.random() - 0.5) * 80\n" +
                "          );\n" +
                "          building.castShadow = true;\n" +
                "          building.receiveShadow = true;\n" +
                "          building.userData = { isMapObject: true, isObstacle: true, width, height, depth };\n" +
                "          this.scene.add(building);\n" +
                "        }\n" +
                "        \n" +
                "        // Add cover objects (barriers, vehicles, etc.)\n" +
                "        for (let i = 0; i < 10; i++) {\n" +
                "          const geo = new THREE.BoxGeometry(2, 1.5, 1);\n" +
                "          const mat = new THREE.MeshStandardMaterial({ color: 0x6a6a7a });\n" +
                "          const cover = new THREE.Mesh(geo, mat);\n" +
                "          cover.position.set(\n" +
                "            (Math.random() - 0.5) * 60,\n" +
                "            0.75,\n" +
                "            (Math.random() - 0.5) * 60\n" +
                "          );\n" +
                "          cover.castShadow = true;\n" +
                "          cover.userData = { isMapObject: true, isObstacle: true };\n" +
                "          this.scene.add(cover);\n" +
                "        }\n" +
                "        \n" +
                "        // Add point lights for atmosphere\n" +
                "        const lightColors = [0xff6600, 0x00d4ff, 0xff0066, 0x00ff88];\n" +
                "        for (let i = 0; i < 5; i++) {\n" +
                "          const light = new THREE.PointLight(lightColors[i % lightColors.length], 1, 20);\n" +
                "          light.position.set(\n" +
                "            (Math.random() - 0.5) * 60,\n" +
                "            3,\n" +
                "            (Math.random() - 0.5) * 60\n" +
                "          );\n" +
                "          light.userData = { isMapObject: true };\n" +
                "          this.scene.add(light);\n" +
                "        }\n" +
                "      }\n" +
                "      \n" +
                "      spawnEnemies() {\n" +
                "        // Spawn enemies based on mission difficulty\n" +
                "        const enemyCount = 5 + Math.floor(Math.random() * 5);\n" +
                "        for (let i = 0; i < enemyCount; i++) {\n" +
                "          this.spawnEnemy();\n" +
                "        }\n" +
                "      }\n" +
                "      \n" +
                "      spawnEnemy() {\n" +
                "        const geo = new THREE.CapsuleGeometry(0.5, 1.8, 4, 8);\n" +
                "        const mat = new THREE.MeshStandardMaterial({ color: 0xff3333 });\n" +
                "        const enemy = new THREE.Mesh(geo, mat);\n" +
                "        enemy.position.set(\n" +
                "          (Math.random() - 0.5) * 60,\n" +
                "          1.7,\n" +
                "          (Math.random() - 0.5) * 60\n" +
                "        );\n" +
                "        enemy.castShadow = true;\n" +
                "        enemy.userData = { \n" +
                "          isEnemy: true, \n" +
                "          health: 100, \n" +
                "          speed: 2 + Math.random() * 2,\n" +
                "          lastShot: 0\n" +
                "        };\n" +
                "        this.scene.add(enemy);\n" +
                "        this.enemies.push(enemy);\n" +
                "      }\n" +
                "      \n" +
                "      shoot() {\n" +
                "        if (!this.isInMatch || !this.isAlive) return;\n" +
                "        \n" +
                "        // Create a projectile\n" +
                "        const geo = new THREE.SphereGeometry(0.1, 4, 4);\n" +
                "        const mat = new THREE.MeshBasicMaterial({ color: 0xffff00 });\n" +
                "        const projectile = new THREE.Mesh(geo, mat);\n" +
                "        projectile.position.copy(this.camera.position);\n" +
                "        \n" +
                "        // Direction from camera\n" +
                "        const direction = new THREE.Vector3(0, 0, -1);\n" +
                "        direction.applyQuaternion(this.camera.quaternion);\n" +
                "        projectile.userData = { \n" +
                "          velocity: direction.multiplyScalar(50),\n" +
                "          life: 2\n" +
                "        };\n" +
                "        this.scene.add(projectile);\n" +
                "        this.projectiles.push(projectile);\n" +
                "        \n" +
                "        // Muzzle flash\n" +
                "        this.showMuzzleFlash();\n" +
                "      }\n" +
                "      \n" +
                "      showMuzzleFlash() {\n" +
                "        const flash = new THREE.PointLight(0xffff00, 2, 5);\n" +
                "        flash.position.copy(this.camera.position);\n" +
                "        const direction = new THREE.Vector3(0, 0, -1);\n" +
                "        direction.applyQuaternion(this.camera.quaternion);\n" +
                "        flash.position.add(direction.multiplyScalar(1));\n" +
                "        this.scene.add(flash);\n" +
                "        setTimeout(() => this.scene.remove(flash), 50);\n" +
                "      }\n" +
                "      \n" +
                "      onMouseMove(event) {\n" +
                "        if (!document.pointerLockElement) return;\n" +
                "        this.playerRotation.yaw -= event.movementX * 0.002;\n" +
                "        this.playerRotation.pitch -= event.movementY * 0.002;\n" +
                "        this.playerRotation.pitch = Math.max(-Math.PI / 2, Math.min(Math.PI / 2, this.playerRotation.pitch));\n" +
                "      }\n" +
                "      \n" +
                "      onPointerLockChange() {\n" +
                "        if (!document.pointerLockElement && this.isInMatch) {\n" +
                "          // Paused - show pause menu\n" +
                "        }\n" +
                "      }\n" +
                "      \n" +
                "      onResize() {\n" +
                "        const width = window.innerWidth;\n" +
                "        const height = window.innerHeight;\n" +
                "        this.camera.aspect = width / height;\n" +
                "        this.camera.updateProjectionMatrix();\n" +
                "        this.renderer.setSize(width, height);\n" +
                "      }\n" +
                "      \n" +
                "      updateWeaponDisplay() {\n" +
                "        if (this.currentWeapon) {\n" +
                "          document.getElementById('weaponName').textContent = this.currentWeapon.name || 'M4 Assault Rifle';\n" +
                "          document.getElementById('ammoCounter').textContent = (this.currentWeapon.magSize || 30) + ' / ' + (this.currentWeapon.reserveAmmo || 120);\n" +
                "        }\n" +
                "      }\n" +
                "      \n" +
                "      updateHealthDisplay() {\n" +
                "        document.getElementById('healthText').textContent = 'Health: ' + Math.max(0, Math.round(this.health));\n" +
                "        document.getElementById('healthFill').style.width = Math.max(0, this.health) + '%';\n" +
                "      }\n" +
                "      \n" +
                "      updateSanityDisplay() {\n" +
                "        document.getElementById('sanityText').textContent = 'Sanity: ' + Math.max(0, Math.round(this.sanity));\n" +
                "        document.getElementById('sanityFill').style.width = Math.max(0, this.sanity) + '%';\n" +
                "      }\n" +
                "      \n" +
                "      updateScoreDisplay() {\n" +
                "        document.getElementById('scoreText').textContent = 'Score: ' + this.score + ' | Kills: ' + this.kills;\n" +
                "      }\n" +
                "      \n" +
                "      addKillFeedEntry(text) {\n" +
                "        const killFeed = document.getElementById('killFeed');\n" +
                "        const item = document.createElement('div');\n" +
                "        item.className = 'kill-feed-item';\n" +
                "        item.textContent = text;\n" +
                "        killFeed.appendChild(item);\n" +
                "        setTimeout(() => item.remove(), 5000);\n" +
                "      }\n" +
                "      \n" +
                "      takeDamage(amount) {\n" +
                "        this.health -= amount;\n" +
                "        this.updateHealthDisplay();\n" +
                "        \n" +
                "        // Damage overlay\n" +
                "        const overlay = document.getElementById('damageOverlay');\n" +
                "        overlay.style.opacity = '1';\n" +
                "        setTimeout(() => overlay.style.opacity = '0', 300);\n" +
                "        \n" +
                "        if (this.health <= 0) {\n" +
                "          this.die();\n" +
                "        }\n" +
                "      }\n" +
                "      \n" +
                "      die() {\n" +
                "        this.isAlive = false;\n" +
                "        this.deaths++;\n" +
                "        document.getElementById('deathScreen').style.display = 'flex';\n" +
                "        document.exitPointerLock();\n" +
                "        \n" +
                "        let respawnTime = 5;\n" +
                "        const timer = setInterval(() => {\n" +
                "          document.getElementById('respawnTimer').textContent = 'Respawning in ' + respawnTime + '...';\n" +
                "          respawnTime--;\n" +
                "          if (respawnTime < 0) {\n" +
                "            clearInterval(timer);\n" +
                "            this.respawn();\n" +
                "          }\n" +
                "        }, 1000);\n" +
                "      }\n" +
                "      \n" +
                "      respawn() {\n" +
                "        this.isAlive = true;\n" +
                "        this.health = 100;\n" +
                "        this.sanity = 100;\n" +
                "        this.playerPosition.set(0, 1.7, 0);\n" +
                "        this.camera.position.copy(this.playerPosition);\n" +
                "        this.updateHealthDisplay();\n" +
                "        this.updateSanityDisplay();\n" +
                "        document.getElementById('deathScreen').style.display = 'none';\n" +
                "        this.canvas.requestPointerLock();\n" +
                "      }\n" +
                "      \n" +
                "      escapeText(text) {\n" +
                "        const div = document.createElement('div');\n" +
                "        div.textContent = text;\n" +
                "        return div.innerHTML;\n" +
                "      }\n" +
                "      \n" +
                "      update(deltaTime) {\n" +
                "        if (!this.isInMatch || !this.isAlive) return;\n" +
                "        \n" +
                "        // Player movement\n" +
                "        const speed = 8 * deltaTime;\n" +
                "        const moveDir = new THREE.Vector3();\n" +
                "        \n" +
                "        if (this.keys['KeyW']) moveDir.z -= 1;\n" +
                "        if (this.keys['KeyS']) moveDir.z += 1;\n" +
                "        if (this.keys['KeyA']) moveDir.x -= 1;\n" +
                "        if (this.keys['KeyD']) moveDir.x += 1;\n" +
                "        \n" +
                "        if (moveDir.length() > 0) {\n" +
                "          moveDir.normalize();\n" +
                "          moveDir.applyAxisAngle(new THREE.Vector3(0, 1, 0), this.playerRotation.yaw);\n" +
                "          this.playerPosition.add(moveDir.multiplyScalar(speed));\n" +
                "          this.playerPosition.y = 1.7; // Keep at eye level\n" +
                "        }\n" +
                "        \n" +
                "        // Update camera\n" +
                "        this.camera.position.copy(this.playerPosition);\n" +
                "        this.camera.rotation.order = 'YXZ';\n" +
                "        this.camera.rotation.y = this.playerRotation.yaw;\n" +
                "        this.camera.rotation.x = this.playerRotation.pitch;\n" +
                "        \n" +
                "        // Update projectiles\n" +
                "        for (let i = this.projectiles.length - 1; i >= 0; i--) {\n" +
                "          const proj = this.projectiles[i];\n" +
                "          proj.position.add(proj.userData.velocity.clone().multiplyScalar(deltaTime));\n" +
                "          proj.userData.life -= deltaTime;\n" +
                "          \n" +
                "          // Check enemy hits\n" +
                "          for (let j = this.enemies.length - 1; j >= 0; j--) {\n" +
                "            const enemy = this.enemies[j];\n" +
                "            if (proj.position.distanceTo(enemy.position) < 1.5) {\n" +
                "              enemy.userData.health -= 30;\n" +
                "              this.scene.remove(proj);\n" +
                "              this.projectiles.splice(i, 1);\n" +
                "              \n" +
                "              if (enemy.userData.health <= 0) {\n" +
                "                this.scene.remove(enemy);\n" +
                "                this.enemies.splice(j, 1);\n" +
                "                this.kills++;\n" +
                "                this.score += 100;\n" +
                "                this.updateScoreDisplay();\n" +
                "                this.addKillFeedEntry('Agent eliminated Mercenary');\n" +
                "                \n" +
                "                // Spawn new enemy\n" +
                "                setTimeout(() => this.spawnEnemy(), 2000);\n" +
                "              }\n" +
                "              break;\n" +
                "            }\n" +
                "          }\n" +
                "          \n" +
                "          // Remove expired projectiles\n" +
                "          if (proj.userData.life <= 0 && this.projectiles.includes(proj)) {\n" +
                "            this.scene.remove(proj);\n" +
                "            this.projectiles.splice(i, 1);\n" +
                "          }\n" +
                "        }\n" +
                "        \n" +
                "        // Update enemies (simple AI)\n" +
                "        this.enemies.forEach(enemy => {\n" +
                "          const dirToPlayer = new THREE.Vector3().subVectors(this.playerPosition, enemy.position);\n" +
                "          const distToPlayer = dirToPlayer.length();\n" +
                "          \n" +
                "          if (distToPlayer < 30) {\n" +
                "            // Move toward player\n" +
                "            dirToPlayer.normalize();\n" +
                "            enemy.position.add(dirToPlayer.multiplyScalar(enemy.userData.speed * deltaTime));\n" +
                "            enemy.lookAt(this.playerPosition.x, enemy.position.y, this.playerPosition.z);\n" +
                "            \n" +
                "            // Shoot at player\n" +
                "            const now = Date.now();\n" +
                "            if (now - enemy.userData.lastShot > 1500 && distToPlayer < 20) {\n" +
                "              enemy.userData.lastShot = now;\n" +
                "              this.takeDamage(5 + Math.random() * 10);\n" +
                "              \n" +
                "              // Sanity drain for supernatural enemies\n" +
                "              if (Math.random() < 0.3) {\n" +
                "                this.sanity -= 2;\n" +
                "                this.updateSanityDisplay();\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        });\n" +
                "        \n" +
                "        // Update minimap\n" +
                "        this.updateMinimap();\n" +
                "      }\n" +
                "      \n" +
                "      updateMinimap() {\n" +
                "        const canvas = document.getElementById('minimapCanvas');\n" +
                "        if (!canvas) return;\n" +
                "        const ctx = canvas.getContext('2d');\n" +
                "        ctx.clearRect(0, 0, 150, 150);\n" +
                "        \n" +
                "        // Draw player\n" +
                "        ctx.fillStyle = '#00d4ff';\n" +
                "        ctx.beginPath();\n" +
                "        ctx.arc(75, 75, 4, 0, Math.PI * 2);\n" +
                "        ctx.fill();\n" +
                "        \n" +
                "        // Draw enemies\n" +
                "        ctx.fillStyle = '#ff3333';\n" +
                "        this.enemies.forEach(enemy => {\n" +
                "          const relX = (enemy.position.x - this.playerPosition.x) * 1.5 + 75;\n" +
                "          const relZ = (enemy.position.z - this.playerPosition.z) * 1.5 + 75;\n" +
                "          if (relX >= 0 && relX <= 150 && relZ >= 0 && relZ <= 150) {\n" +
                "            ctx.beginPath();\n" +
                "            ctx.arc(relX, relZ, 3, 0, Math.PI * 2);\n" +
                "            ctx.fill();\n" +
                "          }\n" +
                "        });\n" +
                "      }\n" +
                "      \n" +
                "      animate() {\n" +
                "        requestAnimationFrame(() => this.animate());\n" +
                "        const deltaTime = Math.min(this.clock.getDelta(), 0.1);\n" +
                "        this.update(deltaTime);\n" +
                "        this.renderer.render(this.scene, this.camera);\n" +
                "      }\n" +
                "    }\n" +
                "    \n" +
                "    // Global game instance\n" +
                "    let game;\n" +
                "    \n" +
                "    // Start mission from briefing\n" +
                "    function startMission() {\n" +
                "      document.getElementById('missionBriefing').style.display = 'none';\n" +
                "      game.startLocalSession();\n" +
                "    }\n" +
                "    \n" +
                "    // Expose selectMission globally\n" +
                "    window.game = null;\n" +
                "    \n" +
                "    // Initialize game on load\n" +
                "    window.addEventListener('DOMContentLoaded', () => {\n" +
                "      game = new Earth2026FpsGame();\n" +
                "      window.game = game;\n" +
                "    });\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(html);
    }

    /**
     * Earth 2026 game HUD page — shown after character lands at a location.
     */
    @GetMapping(value = {"/earth2026/", "/earth2026"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> earth2026GamePage() {
        String html = "<!DOCTYPE html>\n" +
                "<html lang='en'>\n" +
                "<head>\n" +
                "  <meta charset='UTF-8'>\n" +
                "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "  <title>Earth 2026 - Game HUD</title>\n" +
                "  <style>\n" +
                "    * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "    body { background: #0a0a1a; color: #e0e0e0; font-family: 'Segoe UI', system-ui, sans-serif; min-height: 100vh; }\n" +
                "    .header { background: #131926; padding: 16px 24px; border-bottom: 1px solid #1e2840; display: flex; justify-content: space-between; align-items: center; }\n" +
                "    .header h1 { color: #00d4ff; font-size: 1.2em; }\n" +
                "    .header a { color: #9aa0a6; text-decoration: none; font-size: 0.85em; }\n" +
                "    .header a:hover { color: #00d4ff; }\n" +
                "    .container { max-width: 1200px; margin: 24px auto; padding: 0 24px; display: grid; grid-template-columns: 280px 1fr 280px; gap: 24px; }\n" +
                "    .panel { background: #131926; padding: 20px; border-radius: 10px; border: 1px solid #1e2840; }\n" +
                "    .panel h3 { color: #00d4ff; margin-bottom: 16px; font-size: 1em; }\n" +
                "    .panel p { color: #9aa0a6; line-height: 1.6; margin-bottom: 12px; font-size: 0.9em; }\n" +
                "    .center-panel { grid-column: 1 / -1; min-height: 500px; }\n" +
                "    .scene-narrative { background: #131926; padding: 24px; border-radius: 10px; border: 1px solid #1e2840; margin-bottom: 24px; }\n" +
                "    .scene-narrative h2 { color: #00d4ff; margin-bottom: 12px; }\n" +
                "    .scene-narrative p { color: #9aa0a6; line-height: 1.8; margin-bottom: 12px; }\n" +
                "    .actions { display: flex; gap: 12px; margin-top: 16px; flex-wrap: wrap; }\n" +
                "    .action-btn { background: #00d4ff; color: #0a0a1a; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-size: 0.9em; font-weight: 600; }\n" +
                "    .action-btn:hover { background: #00b8d4; }\n" +
                "    .action-btn.secondary { background: #3c4043; color: #e0e0e0; }\n" +
                "    .action-btn.secondary:hover { background: #5f6368; }\n" +
                "    .location-info { background: #151d30; padding: 16px; border-radius: 8px; margin-bottom: 16px; }\n" +
                "    .location-info h4 { color: #00d4ff; margin-bottom: 8px; }\n" +
                "    .location-info p { color: #9aa0a6; font-size: 0.9em; }\n" +
                "    .loading { text-align: center; padding: 40px; color: #9aa0a6; }\n" +
                "    .loading::after { content: '...'; animation: dots 1.5s infinite; }\n" +
                "    @keyframes dots { 0%, 20% { content: '.'; } 40% { content: '..'; } 60%, 100% { content: '...'; } }\n" +
                "    @media (max-width: 900px) { .container { grid-template-columns: 1fr; } }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class='header'>\n" +
                "    <h1>🌍 Earth 2026 — Game HUD</h1>\n" +
                "    <a href='/corporation/'>← Back to Dashboard</a>\n" +
                "  </div>\n" +
                "  <div class='container'>\n" +
                "    <div class='panel' id='leftPanel'>\n" +
                "      <h3>👤 Character</h3>\n" +
                "      <div id='characterInfo' class='loading'>Loading character</div>\n" +
                "    </div>\n" +
                "    <div class='panel' id='rightPanel'>\n" +
                "      <h3>📍 Location</h3>\n" +
                "      <div id='locationInfo' class='loading'>Loading location</div>\n" +
                "    </div>\n" +
                "    <div class='panel center-panel' id='gameArea'>\n" +
                "      <div class='loading' id='loadingMsg'>Loading your adventure</div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  <script>\n" +
                "    var token = localStorage.getItem('accessToken');\n" +
                "    var characterId = localStorage.getItem('earth2026CharacterId');\n" +
                "    var locationName = localStorage.getItem('earth2026Location');\n" +
                "\n" +
                "    function escapeText(t) {\n" +
                "      if (!t) return '';\n" +
                "      return String(t).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\"/g, '&quot;');\n" +
                "    }\n" +
                "\n" +
                "    function loadCharacterInfo() {\n" +
                "      if (!characterId) {\n" +
                "        document.getElementById('characterInfo').innerHTML = '<p>No character selected</p>';\n" +
                "        return;\n" +
                "      }\n" +
                "      var headers = {};\n" +
                "      if (token) headers['Authorization'] = 'Bearer ' + token;\n" +
                "      fetch('/api/v1/earth2026/characters', { headers: headers })\n" +
                "        .then(function(r) { return r.json(); })\n" +
                "        .then(function(chars) {\n" +
                "          var char = (chars || []).find(function(c) { return String(c.id) === String(characterId); });\n" +
                "          if (char) {\n" +
                "            var html = '<div class=\"location-info\">';\n" +
                "            html += '<h4>' + escapeText(char.name || 'Unknown') + '</h4>';\n" +
                "            if (char.description) html += '<p>' + escapeText(char.description) + '</p>';\n" +
                "            html += '</div>';\n" +
                "            document.getElementById('characterInfo').innerHTML = html;\n" +
                "          } else {\n" +
                "            document.getElementById('characterInfo').innerHTML = '<p>Character not found</p>';\n" +
                "          }\n" +
                "        })\n" +
                "        .catch(function(e) {\n" +
                "          console.error('[Earth2026] Failed to load character:', e);\n" +
                "          document.getElementById('characterInfo').innerHTML = '<p>Failed to load character</p>';\n" +
                "        });\n" +
                "    }\n" +
                "\n" +
                "    function loadLocationInfo() {\n" +
                "      if (!locationName) {\n" +
                "        document.getElementById('locationInfo').innerHTML = '<p>No location selected</p>';\n" +
                "        return;\n" +
                "      }\n" +
                "      var headers = {};\n" +
                "      if (token) headers['Authorization'] = 'Bearer ' + token;\n" +
                "      fetch('/api/v1/earth2026/locations', { headers: headers })\n" +
                "        .then(function(r) { return r.json(); })\n" +
                "        .then(function(locations) {\n" +
                "          var loc = (locations || []).find(function(l) { return l.name === locationName; });\n" +
                "          if (loc) {\n" +
                "            var html = '<div class=\"location-info\">';\n" +
                "            html += '<h4>' + escapeText(loc.name) + '</h4>';\n" +
                "            if (loc.type) html += '<p>Type: ' + escapeText(loc.type) + '</p>';\n" +
                "            if (loc.description) html += '<p>' + escapeText(loc.description) + '</p>';\n" +
                "            html += '</div>';\n" +
                "            document.getElementById('locationInfo').innerHTML = html;\n" +
                "          } else {\n" +
                "            var html = '<div class=\"location-info\">';\n" +
                "            html += '<h4>' + escapeText(locationName) + '</h4>';\n" +
                "            html += '</div>';\n" +
                "            document.getElementById('locationInfo').innerHTML = html;\n" +
                "          }\n" +
                "        })\n" +
                "        .catch(function(e) {\n" +
                "          console.error('[Earth2026] Failed to load location:', e);\n" +
                "          document.getElementById('locationInfo').innerHTML = '<p>' + escapeText(locationName) + '</p>';\n" +
                "        });\n" +
                "    }\n" +
                "\n" +
                "    var allScenarios = [];\n" +
                "    var currentScenario = null;\n" +
                "\n" +
                "    function loadScenarios() {\n" +
                "      console.log('[Earth2026] Loading scenarios...');\n" +
                "      var headers = {};\n" +
                "      if (token) headers['Authorization'] = 'Bearer ' + token;\n" +
                "      fetch('/api/v1/earth2026/scenarios?level=1', { headers: headers })\n" +
                "        .then(function(r) { return r.json(); })\n" +
                "        .then(function(scenarios) {\n" +
                "          allScenarios = scenarios || [];\n" +
                "          console.log('[Earth2026] Loaded', allScenarios.length, 'scenarios');\n" +
                "          renderScenarioList();\n" +
                "        })\n" +
                "        .catch(function(e) {\n" +
                "          console.error('[Earth2026] Failed to load scenarios:', e);\n" +
                "          document.getElementById('gameArea').innerHTML = '<div class=\"scene-narrative\"><h2>Welcome to Earth 2026</h2><p>You have landed at ' + escapeText(locationName || 'your destination') + '. Your adventure begins here.</p><p style=\"color:#ff6b6b;\">Failed to load missions. Please try again.</p></div>';\n" +
                "        });\n" +
                "    }\n" +
                "\n" +
                "    function renderScenarioList() {\n" +
                "      var area = document.getElementById('gameArea');\n" +
                "      var available = allScenarios.filter(function(s) { return s.status === 'AVAILABLE'; });\n" +
                "      var inProgress = allScenarios.filter(function(s) { return s.status === 'IN_PROGRESS'; });\n" +
                "      var completed = allScenarios.filter(function(s) { return s.status === 'COMPLETED'; });\n" +
                "      var html = '<div class=\"scene-narrative\">';\n" +
                "      html += '<h2>📋 Available Missions</h2>';\n" +
                "      html += '<p>Choose a mission to begin your adventure at ' + escapeText(locationName || 'your location') + '.</p>';\n" +
                "      if (inProgress.length > 0) {\n" +
                "        html += '<div style=\"background:#1a2332;padding:16px;border-radius:8px;margin-bottom:16px;border-left:3px solid #ff9800;\">';\n" +
                "        html += '<h3 style=\"color:#ff9800;margin-bottom:8px;\">⏳ In Progress</h3>';\n" +
                "        for (var i = 0; i < inProgress.length; i++) {\n" +
                "          html += renderScenarioCard(inProgress[i], true);\n" +
                "        }\n" +
                "        html += '</div>';\n" +
                "      }\n" +
                "      if (available.length === 0 && inProgress.length === 0) {\n" +
                "        html += '<p style=\"color:#9aa0a6;\">No new missions available. Check back later or explore the area.</p>';\n" +
                "      } else {\n" +
                "        for (var i = 0; i < available.length; i++) {\n" +
                "          html += renderScenarioCard(available[i], false);\n" +
                "        }\n" +
                "      }\n" +
                "      if (completed.length > 0) {\n" +
                "        html += '<details style=\"margin-top:16px;\"><summary style=\"color:#4caf50;cursor:pointer;\">✓ Completed Missions (' + completed.length + ')</summary>';\n" +
                "        for (var i = 0; i < completed.length; i++) {\n" +
                "          html += '<div style=\"padding:8px;margin-top:8px;background:#1a2332;border-radius:6px;opacity:0.7;\">';\n" +
                "          html += '<strong>' + escapeText(completed[i].name) + '</strong> — ' + escapeText(completed[i].type) + ' | Difficulty: ' + completed[i].difficulty + ' | XP: ' + completed[i].xpReward + '</div>';\n" +
                "        }\n" +
                "        html += '</details>';\n" +
                "      }\n" +
                "      html += '<div class=\"actions\" style=\"margin-top:16px;\">';\n" +
                "      html += '<button class=\"action-btn\" onclick=\"window.location.href=\\'/earth2026-fps/\\'\">🎮 Enter FPS Mode</button>';\n" +
                "      html += '<button class=\"action-btn\" onclick=\"window.location.href=\\'/earth2026-tycoon/\\'\">🏢 Tycoon Mode</button>';\n" +
                "      html += '<button class=\"action-btn\" onclick=\"window.location.href=\\'/san-diego-map/\\'\">🗺️ 3D Map</button>';\n" +
                "      html += '<button class=\"action-btn secondary\" onclick=\"exploreLocation()\">🔍 Explore Location</button>';\n" +
                "      html += '</div></div>';\n" +
                "      area.innerHTML = html;\n" +
                "    }\n" +
                "\n" +
                "    function renderScenarioCard(s, isInProgress) {\n" +
                "      var html = '<div style=\"background:#151d30;padding:16px;border-radius:8px;margin-bottom:12px;border:1px solid #1e2840;\">';\n" +
                "      html += '<div style=\"display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;\">';\n" +
                "      html += '<strong style=\"color:#00d4ff;font-size:1.1em;\">' + escapeText(s.name) + '</strong>';\n" +
                "      html += '<div>';\n" +
                "      if (s.supernatural) html += '<span style=\"background:#9c27b0;color:white;padding:2px 8px;border-radius:4px;font-size:0.75em;margin-right:4px;\">SUPERNATURAL</span>';\n" +
                "      html += '<span style=\"background:#1e2840;color:#9aa0a6;padding:2px 8px;border-radius:4px;font-size:0.75em;margin-right:4px;\">' + escapeText(s.type) + '</span>';\n" +
                "      html += '<span style=\"background:#1e2840;color:#ff9800;padding:2px 8px;border-radius:4px;font-size:0.75em;\">Difficulty: ' + s.difficulty + '</span>';\n" +
                "      html += '</div></div>';\n" +
                "      if (s.description) html += '<p style=\"color:#9aa0a6;margin-bottom:8px;\">' + escapeText(s.description) + '</p>';\n" +
                "      html += '<div style=\"display:flex;gap:8px;flex-wrap:wrap;font-size:0.85em;color:#9aa0a6;\">';\n" +
                "      html += '<span>⭐ XP Reward: ' + s.xpReward + '</span>';\n" +
                "      if (s.minLevel) html += '<span>📊 Min Level: ' + s.minLevel + '</span>';\n" +
                "      if (s.durationMinutes) html += '<span>⏱ ~' + s.durationMinutes + ' min</span>';\n" +
                "      if (s.location && s.location.name) html += '<span>📍 ' + escapeText(s.location.name) + '</span>';\n" +
                "      if (s.supernatural && s.sanityLoss) html += '<span style=\"color:#ff6b6b;\">🧠 Sanity Risk: ' + s.sanityLoss + '</span>';\n" +
                "      html += '</div>';\n" +
                "      if (isInProgress) {\n" +
                "        html += '<div class=\"actions\" style=\"margin-top:12px;\">';\n" +
                "        html += '<button class=\"action-btn\" onclick=\"enterScenario(\\'' + escapeText(String(s.id)) + '\\')\">▶ Enter Mission</button>';\n" +
                "        html += '<button class=\"action-btn secondary\" onclick=\"resetScenarioApi(\\'' + escapeText(String(s.id)) + '\\')\">↺ Reset</button>';\n" +
                "        html += '</div>';\n" +
                "      } else {\n" +
                "        html += '<div class=\"actions\" style=\"margin-top:12px;\">';\n" +
                "        html += '<button class=\"action-btn\" onclick=\"startScenarioApi(\\'' + escapeText(String(s.id)) + '\\')\">▶ Start Mission</button>';\n" +
                "        html += '</div>';\n" +
                "      }\n" +
                "      html += '</div>';\n" +
                "      return html;\n" +
                "    }\n" +
                "\n" +
                "    function startScenarioApi(scenarioId) {\n" +
                "      console.log('[Earth2026] Starting scenario:', scenarioId);\n" +
                "      var headers = { 'Content-Type': 'application/json' };\n" +
                "      if (token) headers['Authorization'] = 'Bearer ' + token;\n" +
                "      fetch('/api/v1/earth2026/scenarios/' + scenarioId + '/start', {\n" +
                "        method: 'POST',\n" +
                "        headers: headers\n" +
                "      })\n" +
                "        .then(function(r) {\n" +
                "          if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
                "          return r.json();\n" +
                "        })\n" +
                "        .then(function(data) {\n" +
                "          console.log('[Earth2026] Scenario started:', data);\n" +
                "          currentScenario = data;\n" +
                "          // Update the scenario status in our local list\n" +
                "          for (var i = 0; i < allScenarios.length; i++) {\n" +
                "            if (String(allScenarios[i].id) === String(scenarioId)) {\n" +
                "              allScenarios[i].status = 'IN_PROGRESS';\n" +
                "              break;\n" +
                "            }\n" +
                "          }\n" +
                "          // Show the active scenario view\n" +
                "          renderActiveScenario(data);\n" +
                "        })\n" +
                "        .catch(function(e) {\n" +
                "          console.error('[Earth2026] Failed to start scenario:', e);\n" +
                "          alert('Failed to start mission: ' + e.message);\n" +
                "        });\n" +
                "    }\n" +
                "\n" +
                "    function renderActiveScenario(scenario) {\n" +
                "      var area = document.getElementById('gameArea');\n" +
                "      var html = '<div class=\"scene-narrative\">';\n" +
                "      html += '<h2>▶ ' + escapeText(scenario.name) + '</h2>';\n" +
                "      html += '<p style=\"color:#ff9800;font-style:italic;\">Mission in progress...</p>';\n" +
                "      if (scenario.description) html += '<p>' + escapeText(scenario.description) + '</p>';\n" +
                "      html += '<div style=\"background:#151d30;padding:16px;border-radius:8px;margin:16px 0;\">';\n" +
                "      html += '<h4 style=\"color:#00d4ff;margin-bottom:8px;\">Mission Details</h4>';\n" +
                "      html += '<p>Type: ' + escapeText(scenario.type) + ' | Difficulty: ' + scenario.difficulty + ' | XP Reward: ' + scenario.xpReward + '</p>';\n" +
                "      if (scenario.location && scenario.location.name) html += '<p>Location: ' + escapeText(scenario.location.name) + '</p>';\n" +
                "      if (scenario.supernatural) html += '<p style=\"color:#9c27b0;\">⚠ Supernatural elements detected. Proceed with caution.</p>';\n" +
                "      if (scenario.sanityLoss) html += '<p style=\"color:#ff6b6b;\">🧠 Sanity risk: ' + scenario.sanityLoss + ' points on failure</p>';\n" +
                "      html += '</div>';\n" +
                "      html += '<div style=\"background:#1a2332;padding:16px;border-radius:8px;margin:16px 0;border-left:3px solid #00d4ff;\">';\n" +
                "      html += '<h4 style=\"color:#00d4ff;margin-bottom:8px;\">📖 Mission Briefing</h4>';\n" +
                "      html += '<p style=\"line-height:1.8;\">You have accepted the mission: <strong>' + escapeText(scenario.name) + '</strong>.</p>';\n" +
                "      html += '<p style=\"line-height:1.8;\">' + escapeText(scenario.description || 'Investigate and complete the objectives.') + '</p>';\n" +
                "      html += '<p style=\"line-height:1.8;margin-top:12px;\">Choose how you want to proceed with this mission.</p>';\n" +
                "      html += '</div>';\n" +
                "      html += '<div class=\"actions\">';\n" +
                "      html += '<button class=\"action-btn\" onclick=\"completeScenarioApi(\\'' + escapeText(String(scenario.id)) + '\\', true)\">✓ Complete Mission (Success)</button>';\n" +
                "      html += '<button class=\"action-btn\" style=\"background:#ff6b6b;color:white;\" onclick=\"completeScenarioApi(\\'' + escapeText(String(scenario.id)) + '\\', false)\">✗ Fail Mission</button>';\n" +
                "      html += '<button class=\"action-btn secondary\" onclick=\"renderScenarioList()\">← Back to Missions</button>';\n" +
                "      html += '</div></div>';\n" +
                "      area.innerHTML = html;\n" +
                "    }\n" +
                "\n" +
                "    function enterScenario(scenarioId) {\n" +
                "      // Find and display an in-progress scenario\n" +
                "      for (var i = 0; i < allScenarios.length; i++) {\n" +
                "        if (String(allScenarios[i].id) === String(scenarioId)) {\n" +
                "          renderActiveScenario(allScenarios[i]);\n" +
                "          return;\n" +
                "        }\n" +
                "      }\n" +
                "      console.error('[Earth2026] Scenario not found:', scenarioId);\n" +
                "    }\n" +
                "\n" +
                "    function completeScenarioApi(scenarioId, success) {\n" +
                "      console.log('[Earth2026] Completing scenario:', scenarioId, 'Success:', success);\n" +
                "      var headers = { 'Content-Type': 'application/json' };\n" +
                "      if (token) headers['Authorization'] = 'Bearer ' + token;\n" +
                "      fetch('/api/v1/earth2026/scenarios/' + scenarioId + '/complete', {\n" +
                "        method: 'POST',\n" +
                "        headers: headers,\n" +
                "        body: JSON.stringify({ success: success })\n" +
                "      })\n" +
                "        .then(function(r) {\n" +
                "          if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
                "          return r.json();\n" +
                "        })\n" +
                "        .then(function(data) {\n" +
                "          console.log('[Earth2026] Scenario completed:', data);\n" +
                "          // Update local list\n" +
                "          for (var i = 0; i < allScenarios.length; i++) {\n" +
                "            if (String(allScenarios[i].id) === String(scenarioId)) {\n" +
                "              allScenarios[i].status = success ? 'COMPLETED' : 'FAILED';\n" +
                "              break;\n" +
                "            }\n" +
                "          }\n" +
                "          // Show result\n" +
                "          var area = document.getElementById('gameArea');\n" +
                "          var html = '<div class=\"scene-narrative\">';\n" +
                "          if (success) {\n" +
                "            html += '<h2 style=\"color:#4caf50;\">✓ Mission Complete!</h2>';\n" +
                "            html += '<p>Congratulations! You have successfully completed the mission.</p>';\n" +
                "            if (data.xpReward) html += '<p style=\"color:#ff9800;\">⭐ XP Earned: ' + data.xpReward + '</p>';\n" +
                "          } else {\n" +
                "            html += '<h2 style=\"color:#ff6b6b;\">✗ Mission Failed</h2>';\n" +
                "            html += '<p>The mission has failed. The consequences will be felt...</p>';\n" +
                "            if (data.sanityLoss) html += '<p style=\"color:#9c27b0;\">🧠 Sanity Lost: ' + data.sanityLoss + '</p>';\n" +
                "          }\n" +
                "          html += '<div class=\"actions\">';\n" +
                "          html += '<button class=\"action-btn\" onclick=\"renderScenarioList()\">← Back to Missions</button>';\n" +
                "          html += '</div></div>';\n" +
                "          area.innerHTML = html;\n" +
                "        })\n" +
                "        .catch(function(e) {\n" +
                "          console.error('[Earth2026] Failed to complete scenario:', e);\n" +
                "          alert('Failed to update mission status: ' + e.message);\n" +
                "        });\n" +
                "    }\n" +
                "\n" +
                "    function resetScenarioApi(scenarioId) {\n" +
                "      console.log('[Earth2026] Resetting scenario:', scenarioId);\n" +
                "      var headers = { 'Content-Type': 'application/json' };\n" +
                "      if (token) headers['Authorization'] = 'Bearer ' + token;\n" +
                "      fetch('/api/v1/earth2026/scenarios/' + scenarioId + '/reset', {\n" +
                "        method: 'POST',\n" +
                "        headers: headers\n" +
                "      })\n" +
                "        .then(function(r) { return r.json(); })\n" +
                "        .then(function(data) {\n" +
                "          console.log('[Earth2026] Scenario reset:', data);\n" +
                "          for (var i = 0; i < allScenarios.length; i++) {\n" +
                "            if (String(allScenarios[i].id) === String(scenarioId)) {\n" +
                "              allScenarios[i].status = 'AVAILABLE';\n" +
                "              break;\n" +
                "            }\n" +
                "          }\n" +
                "          renderScenarioList();\n" +
                "        })\n" +
                "        .catch(function(e) {\n" +
                "          console.error('[Earth2026] Failed to reset scenario:', e);\n" +
                "        });\n" +
                "    }\n" +
                "\n" +
                "    function exploreLocation() {\n" +
                "      console.log('[Earth2026] Exploring location:', locationName);\n" +
                "      var headers = {};\n" +
                "      if (token) headers['Authorization'] = 'Bearer ' + token;\n" +
                "      fetch('/api/v1/earth2026/locations', { headers: headers })\n" +
                "        .then(function(r) { return r.json(); })\n" +
                "        .then(function(locations) {\n" +
                "          var loc = (locations || []).find(function(l) { return l.name === locationName; });\n" +
                "          var area = document.getElementById('gameArea');\n" +
                "          var html = '<div class=\"scene-narrative\">';\n" +
                "          html += '<h2>🔍 Exploring ' + escapeText(locationName || 'your location') + '</h2>';\n" +
                "          if (loc) {\n" +
                "            if (loc.description) html += '<p>' + escapeText(loc.description) + '</p>';\n" +
                "            if (loc.type) html += '<p>Type: ' + escapeText(loc.type) + '</p>';\n" +
                "            if (loc.city) html += '<p>City: ' + escapeText(loc.city) + '</p>';\n" +
                "            if (loc.country) html += '<p>Country: ' + escapeText(loc.country) + '</p>';\n" +
                "            if (loc.hasPortal) {\n" +
                "              html += '<p style=\"color:#9c27b0;\">🌀 Portal detected! This location has a gateway to another galaxy.</p>';\n" +
                "              if (loc.portalGalaxy) html += '<p style=\"color:#9c27b0;\">Target galaxy: ' + escapeText(loc.portalGalaxy) + '</p>';\n" +
                "            }\n" +
                "            if (loc.secret) html += '<p style=\"color:#ff9800;\">⚠ This is a secret location. Be careful who you tell.</p>';\n" +
                "          } else {\n" +
                "            html += '<p>You look around your surroundings. The area seems ordinary at first glance...</p>';\n" +
                "          }\n" +
                "          html += '<div style=\"background:#151d30;padding:16px;border-radius:8px;margin:16px 0;\">';\n" +
                "          html += '<h4 style=\"color:#00d4ff;margin-bottom:8px;\">🔎 Exploration Options</h4>';\n" +
                "          html += '<p>While exploring, you can:</p>';\n" +
                "          html += '<ul style=\"color:#9aa0a6;margin-left:20px;line-height:2;\">';\n" +
                "          html += '<li>Search for hidden items and clues</li>';\n" +
                "          html += '<li>Talk to NPCs and gather information</li>';\n" +
                "          html += '<li>Look for secret passages or portals</li>';\n" +
                "          html += '<li>Rest and recover before your next mission</li>';\n" +
                "          html += '</ul></div>';\n" +
                "          html += '<div class=\"actions\">';\n" +
                "          html += '<button class=\"action-btn\" onclick=\"renderScenarioList()\">← Back to Missions</button>';\n" +
                "          html += '<button class=\"action-btn secondary\" onclick=\"searchArea()\">🔎 Search Area</button>';\n" +
                "          html += '<button class=\"action-btn secondary\" onclick=\"restAtLocation()\">😴 Rest Here</button>';\n" +
                "          html += '</div></div>';\n" +
                "          area.innerHTML = html;\n" +
                "        })\n" +
                "        .catch(function(e) {\n" +
                "          console.error('[Earth2026] Failed to load location details:', e);\n" +
                "          var area = document.getElementById('gameArea');\n" +
                "          area.innerHTML = '<div class=\"scene-narrative\"><h2>🔍 Exploring ' + escapeText(locationName || 'your location') + '</h2><p>You look around your surroundings...</p><div class=\"actions\"><button class=\"action-btn\" onclick=\"renderScenarioList()\">← Back</button></div></div>';\n" +
                "        });\n" +
                "    }\n" +
                "\n" +
                "    function searchArea() {\n" +
                "      var area = document.getElementById('gameArea');\n" +
                "      var finds = [\n" +
                "        'You find a crumpled note with cryptic symbols...',\n" +
                "        'Behind a loose wall panel, you discover a hidden compartment.',\n" +
                "        'A strange device emits a low hum from beneath a desk.',\n" +
                "        'You notice a map with several locations marked in red.',\n" +
                "        'Under a pile of papers, you find a keycard labeled \"RESTRICTED\".',\n" +
                "        'A flickering light reveals a hidden doorway in the wall.',\n" +
                "        'You discover a journal entry describing strange occurrences.',\n" +
                "        'Nothing unusual here, but you feel watched...'\n" +
                "      ];\n" +
                "      var find = finds[Math.floor(Math.random() * finds.length)];\n" +
                "      var html = '<div class=\"scene-narrative\">';\n" +
                "      html += '<h2>🔎 Search Results</h2>';\n" +
                "      html += '<p>' + find + '</p>';\n" +
                "      html += '<p style=\"color:#4caf50;\">⭐ +10 XP for thorough exploration!</p>';\n" +
                "      html += '<div class=\"actions\">';\n" +
                "      html += '<button class=\"action-btn\" onclick=\"exploreLocation()\">Continue Exploring</button>';\n" +
                "      html += '<button class=\"action-btn secondary\" onclick=\"renderScenarioList()\">← Back to Missions</button>';\n" +
                "      html += '</div></div>';\n" +
                "      area.innerHTML = html;\n" +
                "    }\n" +
                "\n" +
                "    function restAtLocation() {\n" +
                "      var area = document.getElementById('gameArea');\n" +
                "      var html = '<div class=\"scene-narrative\">';\n" +
                "      html += '<h2>😴 Resting...</h2>';\n" +
                "      html += '<p>You find a quiet spot to rest and recover your strength.</p>';\n" +
                "      html += '<p style=\"color:#4caf50;\">Your energy is restored. You feel ready for the next challenge.</p>';\n" +
                "      html += '<div class=\"actions\">';\n" +
                "      html += '<button class=\"action-btn\" onclick=\"renderScenarioList()\">← Back to Missions</button>';\n" +
                "      html += '</div></div>';\n" +
                "      area.innerHTML = html;\n" +
                "    }\n" +
                "\n" +
                "    // Initialize\n" +
                "    console.log('[Earth2026] Initializing game HUD...');\n" +
                "    loadCharacterInfo();\n" +
                "    loadLocationInfo();\n" +
                "    loadScenarios();\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(html);
    }

    /**
     * Earth 2026 Corporation Tycoon page — business management game mode.
     * Build buildings, hire employees, complete missions, and grow your corporation.
     */
    @GetMapping(value = "/earth2026-tycoon/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> earth2026TycoonPage() {
        String html = "<!DOCTYPE html>\n" +
                "<html lang='en'>\n" +
                "<head>\n" +
                "  <meta charset='UTF-8'>\n" +
                "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "  <title>Earth 2026 - Corporation Tycoon</title>\n" +
                "  <style>\n" +
                "    * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "    body { background: #0a0e17; color: #e0e0e0; font-family: 'Segoe UI', system-ui, sans-serif; }\n" +
                "    .header { background: #131926; padding: 12px 24px; border-bottom: 1px solid #1e2840; display: flex; justify-content: space-between; align-items: center; }\n" +
                "    .header h1 { color: #00d4ff; font-size: 1.2em; }\n" +
                "    .header .nav { display: flex; gap: 15px; }\n" +
                "    .header a { color: #9aa0a6; text-decoration: none; font-size: 0.85em; padding: 5px 10px; border-radius: 4px; transition: all 0.2s; }\n" +
                "    .header a:hover { color: #00d4ff; background: rgba(0,212,255,0.1); }\n" +
                "    .main { display: grid; grid-template-columns: 280px 1fr 300px; gap: 20px; padding: 20px; min-height: calc(100vh - 60px); }\n" +
                "    .panel { background: #131926; border: 1px solid #1e2840; border-radius: 8px; padding: 15px; }\n" +
                "    .panel h2 { color: #00d4ff; font-size: 1em; margin-bottom: 12px; border-bottom: 1px solid #1e2840; padding-bottom: 8px; }\n" +
                "    .panel h3 { color: #9aa0a6; font-size: 0.85em; margin: 10px 0 6px; }\n" +
                "    .resource-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 15px; }\n" +
                "    .resource-item { background: #1a2035; padding: 10px; border-radius: 6px; text-align: center; }\n" +
                "    .resource-item .value { color: #00d4ff; font-size: 1.3em; font-weight: bold; }\n" +
                "    .resource-item .label { color: #6a7080; font-size: 0.75em; margin-top: 2px; }\n" +
                "    .resource-item .rate { color: #4ade80; font-size: 0.7em; }\n" +
                "    .level-bar { background: #1a2035; border-radius: 6px; padding: 10px; margin-bottom: 15px; }\n" +
                "    .level-bar .level { color: #fbbf24; font-size: 1.1em; font-weight: bold; }\n" +
                "    .level-bar .xp { color: #6a7080; font-size: 0.8em; }\n" +
                "    .xp-bar { background: #1e2840; height: 6px; border-radius: 3px; margin-top: 6px; overflow: hidden; }\n" +
                "    .xp-fill { background: linear-gradient(90deg, #fbbf24, #f59e0b); height: 100%; transition: width 0.3s; }\n" +
                "    .building-card { background: #1a2035; padding: 12px; border-radius: 6px; margin-bottom: 8px; border-left: 3px solid #00d4ff; }\n" +
                "    .building-card .name { color: #e0e0e0; font-weight: bold; font-size: 0.9em; }\n" +
                "    .building-card .level { color: #fbbf24; font-size: 0.8em; }\n" +
                "    .building-card .stats { color: #6a7080; font-size: 0.75em; margin-top: 4px; }\n" +
                "    .building-card .actions { margin-top: 8px; display: flex; gap: 6px; }\n" +
                "    .btn { background: #1e2840; color: #e0e0e0; border: 1px solid #2a3a5c; padding: 5px 12px; border-radius: 4px; cursor: pointer; font-size: 0.8em; transition: all 0.2s; }\n" +
                "    .btn:hover { background: #2a3a5c; border-color: #00d4ff; }\n" +
                "    .btn-primary { background: #00d4ff; color: #0a0e17; border: none; font-weight: bold; }\n" +
                "    .btn-primary:hover { background: #00b8d4; }\n" +
                "    .btn-success { background: #4ade80; color: #0a0e17; border: none; font-weight: bold; }\n" +
                "    .btn-success:hover { background: #22c55e; }\n" +
                "    .btn-danger { background: #ef4444; color: #fff; border: none; }\n" +
                "    .btn-danger:hover { background: #dc2626; }\n" +
                "    .btn:disabled { opacity: 0.5; cursor: not-allowed; }\n" +
                "    .employee-card { background: #1a2035; padding: 10px; border-radius: 6px; margin-bottom: 6px; display: flex; justify-content: space-between; align-items: center; }\n" +
                "    .employee-card .name { color: #e0e0e0; font-size: 0.85em; }\n" +
                "    .employee-card .role { color: #6a7080; font-size: 0.75em; }\n" +
                "    .employee-card .salary { color: #fbbf24; font-size: 0.75em; }\n" +
                "    .mission-card { background: #1a2035; padding: 12px; border-radius: 6px; margin-bottom: 8px; border-left: 3px solid #4ade80; }\n" +
                "    .mission-card .title { color: #e0e0e0; font-weight: bold; font-size: 0.9em; }\n" +
                "    .mission-card .desc { color: #6a7080; font-size: 0.8em; margin-top: 4px; }\n" +
                "    .mission-card .reward { color: #fbbf24; font-size: 0.8em; margin-top: 4px; }\n" +
                "    .mission-card .actions { margin-top: 8px; }\n" +
                "    .research-card { background: #1a2035; padding: 10px; border-radius: 6px; margin-bottom: 6px; border-left: 3px solid #8b5cf6; }\n" +
                "    .research-card .name { color: #e0e0e0; font-size: 0.85em; }\n" +
                "    .research-card .progress { color: #6a7080; font-size: 0.75em; margin-top: 4px; }\n" +
                "    .progress-bar { background: #1e2840; height: 4px; border-radius: 2px; margin-top: 4px; overflow: hidden; }\n" +
                "    .progress-fill { background: #8b5cf6; height: 100%; transition: width 0.3s; }\n" +
                "    .tab-bar { display: flex; gap: 4px; margin-bottom: 15px; }\n" +
                "    .tab { background: #1a2035; color: #6a7080; border: 1px solid #1e2840; padding: 6px 14px; border-radius: 4px; cursor: pointer; font-size: 0.8em; transition: all 0.2s; }\n" +
                "    .tab.active { background: #00d4ff; color: #0a0e17; border-color: #00d4ff; }\n" +
                "    .tab:hover:not(.active) { border-color: #00d4ff; color: #00d4ff; }\n" +
                "    .modal { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.7); display: flex; align-items: center; justify-content: center; z-index: 100; }\n" +
                "    .modal-content { background: #131926; border: 1px solid #1e2840; border-radius: 10px; padding: 25px; max-width: 500px; width: 90%; max-height: 80vh; overflow-y: auto; }\n" +
                "    .modal-content h2 { color: #00d4ff; margin-bottom: 15px; }\n" +
                "    .modal-content .close { float: right; color: #6a7080; cursor: pointer; font-size: 1.2em; }\n" +
                "    .modal-content .close:hover { color: #ef4444; }\n" +
                "    .form-group { margin-bottom: 12px; }\n" +
                "    .form-group label { display: block; color: #6a7080; font-size: 0.8em; margin-bottom: 4px; }\n" +
                "    .form-group input, .form-group select { width: 100%; background: #1a2035; border: 1px solid #1e2840; color: #e0e0e0; padding: 8px; border-radius: 4px; }\n" +
                "    .notification { position: fixed; top: 20px; right: 20px; background: #131926; border: 1px solid #00d4ff; border-radius: 8px; padding: 12px 20px; z-index: 200; animation: slideIn 0.3s ease; }\n" +
                "    @keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }\n" +
                "    .scrollable { max-height: calc(100vh - 200px); overflow-y: auto; }\n" +
                "    .scrollable::-webkit-scrollbar { width: 6px; }\n" +
                "    .scrollable::-webkit-scrollbar-track { background: #1a2035; }\n" +
                "    .scrollable::-webkit-scrollbar-thumb { background: #2a3a5c; border-radius: 3px; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class='header'>\n" +
                "    <h1>🏢 Corporation Tycoon</h1>\n" +
                "    <div class='nav'>\n" +
                "      <a href='/earth2026/'>🌍 Earth 2026</a>\n" +
                "      <a href='/earth2026-fps/'>🎮 FPS Mode</a>\n" +
                "      <a href='/corporation/'>📊 Dashboard</a>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "  <div class='main'>\n" +
                "    <div class='left-panel scrollable'>\n" +
                "      <div class='panel'>\n" +
                "        <h2>📊 Resources</h2>\n" +
                "        <div class='resource-grid'>\n" +
                "          <div class='resource-item'><div class='value' id='credits'>0</div><div class='label'>Credits</div><div class='rate' id='creditsRate'>+0/hr</div></div>\n" +
                "          <div class='resource-item'><div class='value' id='influence'>0</div><div class='label'>Influence</div><div class='rate' id='influenceRate'>+0/hr</div></div>\n" +
                "          <div class='resource-item'><div class='value' id='tech'>0</div><div class='label'>Tech</div><div class='rate' id='techRate'>+0/hr</div></div>\n" +
                "          <div class='resource-item'><div class='value' id='materials'>0</div><div class='label'>Materials</div><div class='rate' id='materialsRate'>+0/hr</div></div>\n" +
                "        </div>\n" +
                "        <div class='level-bar'>\n" +
                "          <div class='level'>Level <span id='level'>1</span></div>\n" +
                "          <div class='xp'><span id='xp'>0</span> / <span id='xpNeeded'>1000</span> XP</div>\n" +
                "          <div class='xp-bar'><div class='xp-fill' id='xpBar' style='width:0%'></div></div>\n" +
                "        </div>\n" +
                "        <h3>📈 Stats</h3>\n" +
                "        <div style='color:#6a7080;font-size:0.8em'>\n" +
                "          <div>Reputation: <span id='reputation' style='color:#fbbf24'>50</span></div>\n" +
                "          <div>Missions: <span id='missionsCompleted' style='color:#4ade80'>0</span></div>\n" +
                "          <div>Employees: <span id='employeeCount' style='color:#00d4ff'>0</span></div>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "    <div class='center-panel scrollable'>\n" +
                "      <div class='panel'>\n" +
                "        <div class='tab-bar'>\n" +
                "          <div class='tab active' onclick='switchTab(\"buildings\")'>🏗️ Buildings</div>\n" +
                "          <div class='tab' onclick='switchTab(\"employees\")'>👥 Employees</div>\n" +
                "          <div class='tab' onclick='switchTab(\"missions\")'>🎯 Missions</div>\n" +
                "          <div class='tab' onclick='switchTab(\"research\")'>🔬 Research</div>\n" +
                "        </div>\n" +
                "        <div id='buildingsTab'>\n" +
                "          <div style='display:flex;justify-content:space-between;align-items:center;margin-bottom:12px'>\n" +
                "            <h2 style='color:#00d4ff;font-size:1em'>🏗️ Your Buildings</h2>\n" +
                "            <button class='btn btn-primary' onclick='showBuildModal()'>+ Construct</button>\n" +
                "          </div>\n" +
                "          <div id='buildingsList'></div>\n" +
                "        </div>\n" +
                "        <div id='employeesTab' style='display:none'>\n" +
                "          <div style='display:flex;justify-content:space-between;align-items:center;margin-bottom:12px'>\n" +
                "            <h2 style='color:#00d4ff;font-size:1em'>👥 Your Employees</h2>\n" +
                "            <button class='btn btn-primary' onclick='showHireModal()'>+ Hire</button>\n" +
                "          </div>\n" +
                "          <div id='employeesList'></div>\n" +
                "        </div>\n" +
                "        <div id='missionsTab' style='display:none'>\n" +
                "          <h2 style='color:#00d4ff;font-size:1em;margin-bottom:12px'>🎯 Available Missions</h2>\n" +
                "          <div id='missionsList'></div>\n" +
                "        </div>\n" +
                "        <div id='researchTab' style='display:none'>\n" +
                "          <div style='display:flex;justify-content:space-between;align-items:center;margin-bottom:12px'>\n" +
                "            <h2 style='color:#00d4ff;font-size:1em'>🔬 Research Projects</h2>\n" +
                "            <button class='btn btn-primary' onclick='showResearchModal()'>+ Start Research</button>\n" +
                "          </div>\n" +
                "          <div id='researchList'></div>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "    <div class='right-panel scrollable'>\n" +
                "      <div class='panel'>\n" +
                "        <h2>📋 Activity Log</h2>\n" +
                "        <div id='activityLog' style='max-height:calc(100vh - 150px);overflow-y:auto'></div>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "  <div id='buildModal' class='modal' style='display:none'>\n" +
                "    <div class='modal-content'>\n" +
                "      <span class='close' onclick='closeModal(\"buildModal\")'>&times;</span>\n" +
                "      <h2>🏗️ Construct Building</h2>\n" +
                "      <div id='buildOptions'></div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "  <div id='hireModal' class='modal' style='display:none'>\n" +
                "    <div class='modal-content'>\n" +
                "      <span class='close' onclick='closeModal(\"hireModal\")'>&times;</span>\n" +
                "      <h2>👥 Hire Employee</h2>\n" +
                "      <div class='form-group'><label>Name</label><input type='text' id='hireName' placeholder='Employee name'></div>\n" +
                "      <div class='form-group'><label>Role</label><select id='hireRole'></select></div>\n" +
                "      <div class='form-group'><label>Salary</label><input type='number' id='hireSalary' value='500'></div>\n" +
                "      <button class='btn btn-success' onclick='hireEmployee()' style='width:100%'>Hire Employee</button>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "  <div id='researchModal' class='modal' style='display:none'>\n" +
                "    <div class='modal-content'>\n" +
                "      <span class='close' onclick='closeModal(\"researchModal\")'>&times;</span>\n" +
                "      <h2>🔬 Start Research</h2>\n" +
                "      <div id='researchOptions'></div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "  <script>\n" +
                "    // ===== Corporation Tycoon Game =====\n" +
                "    \n" +
                "    const CORPORATION_ID = 1; // Default corporation\n" +
                "    let dashboard = null;\n" +
                "    let activityLog = [];\n" +
                "    \n" +
                "    // Load dashboard data\n" +
                "    async function loadDashboard() {\n" +
                "      try {\n" +
                "        const resp = await fetch(`/api/v1/tycoon/corporations/${CORPORATION_ID}`);\n" +
                "        dashboard = await resp.json();\n" +
                "        updateUI();\n" +
                "        addActivity('Dashboard loaded');\n" +
                "      } catch (e) {\n" +
                "        console.error('[Tycoon] Failed to load dashboard:', e);\n" +
                "        addActivity('Failed to load dashboard');\n" +
                "      }\n" +
                "    }\n" +
                "    \n" +
                "    function updateUI() {\n" +
                "      if (!dashboard) return;\n" +
                "      \n" +
                "      // Resources\n" +
                "      document.getElementById('credits').textContent = formatNumber(dashboard.credits);\n" +
                "      document.getElementById('influence').textContent = formatNumber(dashboard.influence);\n" +
                "      document.getElementById('tech').textContent = formatNumber(dashboard.tech);\n" +
                "      document.getElementById('materials').textContent = formatNumber(dashboard.materials);\n" +
                "      document.getElementById('creditsRate').textContent = `+${formatNumber(dashboard.creditsPerHour)}/hr`;\n" +
                "      \n" +
                "      // Level\n" +
                "      document.getElementById('level').textContent = dashboard.level;\n" +
                "      document.getElementById('xp').textContent = formatNumber(dashboard.experience);\n" +
                "      document.getElementById('xpNeeded').textContent = formatNumber(dashboard.experienceToNextLevel);\n" +
                "      const xpPercent = (dashboard.experience / dashboard.experienceToNextLevel) * 100;\n" +
                "      document.getElementById('xpBar').style.width = xpPercent + '%';\n" +
                "      \n" +
                "      // Stats\n" +
                "      document.getElementById('reputation').textContent = dashboard.reputation;\n" +
                "      document.getElementById('missionsCompleted').textContent = dashboard.missionsCompleted;\n" +
                "      document.getElementById('employeeCount').textContent = dashboard.employees ? dashboard.employees.length : 0;\n" +
                "      \n" +
                "      // Buildings\n" +
                "      renderBuildings();\n" +
                "      \n" +
                "      // Employees\n" +
                "      renderEmployees();\n" +
                "      \n" +
                "      // Research\n" +
                "      renderResearch();\n" +
                "    }\n" +
                "    \n" +
                "    function formatNumber(n) {\n" +
                "      if (n >= 1000000) return (n / 1000000).toFixed(1) + 'M';\n" +
                "      if (n >= 1000) return (n / 1000).toFixed(1) + 'K';\n" +
                "      return n.toString();\n" +
                "    }\n" +
                "    \n" +
                "    function renderBuildings() {\n" +
                "      const list = document.getElementById('buildingsList');\n" +
                "      if (!dashboard.buildings || dashboard.buildings.length === 0) {\n" +
                "        list.innerHTML = '<div style=\"color:#6a7080;text-align:center;padding:20px\">No buildings yet. Construct your first building!</div>';\n" +
                "        return;\n" +
                "      }\n" +
                "      \n" +
                "      list.innerHTML = dashboard.buildings.map(b => `\n" +
                "        <div class='building-card'>\n" +
                "          <div class='name'>${b.displayName}</div>\n" +
                "          <div class='level'>Level ${b.level} / ${b.maxLevel}</div>\n" +
                "          <div class='stats'>\n" +
                "            Credits: +${b.creditsPerHour}/hr | Influence: +${b.influencePerHour}/hr | \n" +
                "            Tech: +${b.techPerHour}/hr | Materials: +${b.materialsPerHour}/hr | \n" +
                "            Slots: ${b.employeeSlots}\n" +
                "          </div>\n" +
                "          <div class='actions'>\n" +
                "            ${b.upgradeCost > 0 ? `<button class='btn' onclick='upgradeBuilding(\"${b.type}\")'>Upgrade (${formatNumber(b.upgradeCost)} credits)</button>` : '<span style=\"color:#4ade80;font-size:0.75em\">MAX LEVEL</span>'}\n" +
                "          </div>\n" +
                "        </div>\n" +
                "      `).join('');\n" +
                "    }\n" +
                "    \n" +
                "    function renderEmployees() {\n" +
                "      const list = document.getElementById('employeesList');\n" +
                "      if (!dashboard.employees || dashboard.employees.length === 0) {\n" +
                "        list.innerHTML = '<div style=\"color:#6a7080;text-align:center;padding:20px\">No employees yet. Hire your first employee!</div>';\n" +
                "        return;\n" +
                "      }\n" +
                "      \n" +
                "      list.innerHTML = dashboard.employees.map(e => `\n" +
                "        <div class='employee-card'>\n" +
                "          <div>\n" +
                "            <div class='name'>${e.name}</div>\n" +
                "            <div class='role'>${e.role} | Efficiency: ${e.efficiency}%</div>\n" +
                "          </div>\n" +
                "          <div class='salary'>${formatNumber(e.dailySalary)}/day</div>\n" +
                "        </div>\n" +
                "      `).join('');\n" +
                "    }\n" +
                "    \n" +
                "    function renderResearch() {\n" +
                "      const list = document.getElementById('researchList');\n" +
                "      if (!dashboard.researchProjects || dashboard.researchProjects.length === 0) {\n" +
                "        list.innerHTML = '<div style=\"color:#6a7080;text-align:center;padding:20px\">No research projects. Start your first research!</div>';\n" +
                "        return;\n" +
                "      }\n" +
                "      \n" +
                "      list.innerHTML = dashboard.researchProjects.map(r => `\n" +
                "        <div class='research-card'>\n" +
                "          <div class='name'>${r.name} ${r.completed ? '✅' : ''}</div>\n" +
                "          <div class='progress'>Progress: ${r.progress.toFixed(1)}%</div>\n" +
                "          <div class='progress-bar'><div class='progress-fill' style='width:${r.progress}%'></div></div>\n" +
                "        </div>\n" +
                "      `).join('');\n" +
                "    }\n" +
                "    \n" +
                "    // Load missions from Earth 2026 scenarios\n" +
                "    async function loadMissions() {\n" +
                "      try {\n" +
                "        const resp = await fetch('/api/v1/earth2026/scenarios?level=1');\n" +
                "        const scenarios = await resp.json();\n" +
                "        const list = document.getElementById('missionsList');\n" +
                "        \n" +
                "        if (!scenarios || scenarios.length === 0) {\n" +
                "          list.innerHTML = '<div style=\"color:#6a7080;text-align:center;padding:20px\">No missions available</div>';\n" +
                "          return;\n" +
                "        }\n" +
                "        \n" +
                "        list.innerHTML = scenarios.map(s => `\n" +
                "          <div class='mission-card'>\n" +
                "            <div class='title'>${s.title || s.name || 'Mission'}</div>\n" +
                "            <div class='desc'>${s.description || 'Complete this mission to earn credits and XP'}</div>\n" +
                "            <div class='reward'>Difficulty: ${s.difficulty || 1} | XP: ${s.xpReward || 100}</div>\n" +
                "            <div class='actions'>\n" +
                "              <button class='btn btn-success' onclick='completeMission(${s.id}, ${s.difficulty || 1})'>Complete Mission</button>\n" +
                "            </div>\n" +
                "          </div>\n" +
                "        `).join('');\n" +
                "      } catch (e) {\n" +
                "        console.warn('[Tycoon] Failed to load missions:', e);\n" +
                "        document.getElementById('missionsList').innerHTML = '<div style=\"color:#6a7080;text-align:center;padding:20px\">Failed to load missions</div>';\n" +
                "      }\n" +
                "    }\n" +
                "    \n" +
                "    // Tab switching\n" +
                "    function switchTab(tab) {\n" +
                "      document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));\n" +
                "      event.target.classList.add('active');\n" +
                "      \n" +
                "      document.getElementById('buildingsTab').style.display = 'none';\n" +
                "      document.getElementById('employeesTab').style.display = 'none';\n" +
                "      document.getElementById('missionsTab').style.display = 'none';\n" +
                "      document.getElementById('researchTab').style.display = 'none';\n" +
                "      \n" +
                "      document.getElementById(tab + 'Tab').style.display = 'block';\n" +
                "    }\n" +
                "    \n" +
                "    // Modal functions\n" +
                "    function closeModal(id) {\n" +
                "      document.getElementById(id).style.display = 'none';\n" +
                "    }\n" +
                "    \n" +
                "    async function showBuildModal() {\n" +
                "      try {\n" +
                "        const resp = await fetch(`/api/v1/tycoon/corporations/${CORPORATION_ID}/buildings-available`);\n" +
                "        const buildings = await resp.json();\n" +
                "        const options = document.getElementById('buildOptions');\n" +
                "        \n" +
                "        if (buildings.length === 0) {\n" +
                "          options.innerHTML = '<div style=\"color:#6a7080;text-align:center;padding:20px\">All buildings constructed!</div>';\n" +
                "        } else {\n" +
                "          options.innerHTML = buildings.map(b => `\n" +
                "            <div class='building-card' style='margin-bottom:10px'>\n" +
                "              <div class='name'>${b.displayName}</div>\n" +
                "              <div class='stats'>${b.description}</div>\n" +
                "              <div class='actions' style='margin-top:8px'>\n" +
                "                <button class='btn btn-primary' onclick='constructBuilding(\"${b.type}\")'>Construct (${formatNumber(b.constructionCost)} credits)</button>\n" +
                "              </div>\n" +
                "            </div>\n" +
                "          `).join('');\n" +
                "        }\n" +
                "        document.getElementById('buildModal').style.display = 'flex';\n" +
                "      } catch (e) {\n" +
                "        console.error('[Tycoon] Failed to load buildings:', e);\n" +
                "      }\n" +
                "    }\n" +
                "    \n" +
                "    async function showHireModal() {\n" +
                "      try {\n" +
                "        const resp = await fetch('/api/v1/tycoon/employee-roles');\n" +
                "        const roles = await resp.json();\n" +
                "        const select = document.getElementById('hireRole');\n" +
                "        select.innerHTML = roles.map(r => `<option value='${r.role}'>${r.displayName} (Credits: +${r.creditBonus}, Influence: +${r.influenceBonus})</option>`).join('');\n" +
                "        document.getElementById('hireModal').style.display = 'flex';\n" +
                "      } catch (e) {\n" +
                "        console.error('[Tycoon] Failed to load roles:', e);\n" +
                "      }\n" +
                "    }\n" +
                "    \n" +
                "    async function showResearchModal() {\n" +
                "      try {\n" +
                "        const resp = await fetch('/api/v1/tycoon/research-available');\n" +
                "        const research = await resp.json();\n" +
                "        const options = document.getElementById('researchOptions');\n" +
                "        options.innerHTML = research.map(r => `\n" +
                "          <div class='research-card' style='margin-bottom:10px'>\n" +
                "            <div class='name'>${r.displayName}</div>\n" +
                "            <div class='progress'>${r.description}</div>\n" +
                "            <div class='actions' style='margin-top:8px'>\n" +
                "              <button class='btn btn-primary' onclick='startResearch(\"${r.type}\")'>Start Research (${formatNumber(r.baseCost)} credits)</button>\n" +
                "            </div>\n" +
                "          </div>\n" +
                "        `).join('');\n" +
                "        document.getElementById('researchModal').style.display = 'flex';\n" +
                "      } catch (e) {\n" +
                "        console.error('[Tycoon] Failed to load research:', e);\n" +
                "      }\n" +
                "    }\n" +
                "    \n" +
                "    // Actions\n" +
                "    async function constructBuilding(type) {\n" +
                "      try {\n" +
                "        const resp = await fetch(`/api/v1/tycoon/corporations/${CORPORATION_ID}/buildings`, {\n" +
                "          method: 'POST',\n" +
                "          headers: { 'Content-Type': 'application/json' },\n" +
                "          body: JSON.stringify({ type })\n" +
                "        });\n" +
                "        const result = await resp.json();\n" +
                "        if (result.success) {\n" +
                "          addActivity(`Constructed ${type}`);\n" +
                "          closeModal('buildModal');\n" +
                "          await loadDashboard();\n" +
                "          showNotification('Building constructed!');\n" +
                "        } else {\n" +
                "          showNotification(result.message || 'Failed to construct building');\n" +
                "        }\n" +
                "      } catch (e) {\n" +
                "        console.error('[Tycoon] Failed to construct:', e);\n" +
                "      }\n" +
                "    }\n" +
                "    \n" +
                "    async function upgradeBuilding(type) {\n" +
                "      try {\n" +
                "        const resp = await fetch(`/api/v1/tycoon/corporations/${CORPORATION_ID}/buildings/${type}`, {\n" +
                "          method: 'PUT'\n" +
                "        });\n" +
                "        const result = await resp.json();\n" +
                "        if (result.success) {\n" +
                "          addActivity(`Upgraded ${type}`);\n" +
                "          await loadDashboard();\n" +
                "          showNotification('Building upgraded!');\n" +
                "        } else {\n" +
                "          showNotification(result.message || 'Failed to upgrade building');\n" +
                "        }\n" +
                "      } catch (e) {\n" +
                "        console.error('[Tycoon] Failed to upgrade:', e);\n" +
                "      }\n" +
                "    }\n" +
                "    \n" +
                "    async function hireEmployee() {\n" +
                "      const name = document.getElementById('hireName').value;\n" +
                "      const role = document.getElementById('hireRole').value;\n" +
                "      const salary = parseInt(document.getElementById('hireSalary').value);\n" +
                "      \n" +
                "      if (!name) {\n" +
                "        showNotification('Please enter employee name');\n" +
                "        return;\n" +
                "      }\n" +
                "      \n" +
                "      try {\n" +
                "        const resp = await fetch(`/api/v1/tycoon/corporations/${CORPORATION_ID}/employees`, {\n" +
                "          method: 'POST',\n" +
                "          headers: { 'Content-Type': 'application/json' },\n" +
                "          body: JSON.stringify({ name, role, salary })\n" +
                "        });\n" +
                "        const result = await resp.json();\n" +
                "        if (result.success) {\n" +
                "          addActivity(`Hired ${name} as ${role}`);\n" +
                "          closeModal('hireModal');\n" +
                "          await loadDashboard();\n" +
                "          showNotification('Employee hired!');\n" +
                "        } else {\n" +
                "          showNotification(result.message || 'Failed to hire employee');\n" +
                "        }\n" +
                "      } catch (e) {\n" +
                "        console.error('[Tycoon] Failed to hire:', e);\n" +
                "      }\n" +
                "    }\n" +
                "    \n" +
                "    async function completeMission(scenarioId, difficulty) {\n" +
                "      try {\n" +
                "        const kills = Math.floor(Math.random() * 10) + 1;\n" +
                "        const score = Math.floor(Math.random() * 5000) + 1000;\n" +
                "        \n" +
                "        const resp = await fetch(`/api/v1/tycoon/corporations/${CORPORATION_ID}/missions`, {\n" +
                "          method: 'POST',\n" +
                "          headers: { 'Content-Type': 'application/json' },\n" +
                "          body: JSON.stringify({ difficulty, kills, score })\n" +
                "        });\n" +
                "        const result = await resp.json();\n" +
                "        if (result.success) {\n" +
                "          addActivity(`Mission completed: +${formatNumber(result.creditsEarned)} credits, +${result.experienceEarned} XP`);\n" +
                "          await loadDashboard();\n" +
                "          showNotification(`Mission complete! +${formatNumber(result.creditsEarned)} credits`);\n" +
                "        }\n" +
                "      } catch (e) {\n" +
                "        console.error('[Tycoon] Failed to complete mission:', e);\n" +
                "      }\n" +
                "    }\n" +
                "    \n" +
                "    async function startResearch(type) {\n" +
                "      try {\n" +
                "        const cost = 10000; // Default cost\n" +
                "        const resp = await fetch(`/api/v1/tycoon/corporations/${CORPORATION_ID}/research`, {\n" +
                "          method: 'POST',\n" +
                "          headers: { 'Content-Type': 'application/json' },\n" +
                "          body: JSON.stringify({ name: type, cost })\n" +
                "        });\n" +
                "        const result = await resp.json();\n" +
                "        if (result.success) {\n" +
                "          addActivity(`Started research: ${type}`);\n" +
                "          closeModal('researchModal');\n" +
                "          await loadDashboard();\n" +
                "          showNotification('Research started!');\n" +
                "        } else {\n" +
                "          showNotification(result.message || 'Failed to start research');\n" +
                "        }\n" +
                "      } catch (e) {\n" +
                "        console.error('[Tycoon] Failed to start research:', e);\n" +
                "      }\n" +
                "    }\n" +
                "    \n" +
                "    // Activity log\n" +
                "    function addActivity(message) {\n" +
                "      const time = new Date().toLocaleTimeString();\n" +
                "      activityLog.unshift({ time, message });\n" +
                "      if (activityLog.length > 50) activityLog.pop();\n" +
                "      \n" +
                "      const log = document.getElementById('activityLog');\n" +
                "      log.innerHTML = activityLog.map(a => `\n" +
                "        <div style='padding:6px 0;border-bottom:1px solid #1e2840;font-size:0.8em'>\n" +
                "          <span style='color:#6a7080'>[${a.time}]</span> <span style='color:#e0e0e0'>${a.message}</span>\n" +
                "        </div>\n" +
                "      `).join('');\n" +
                "    }\n" +
                "    \n" +
                "    // Notifications\n" +
                "    function showNotification(message) {\n" +
                "      const notif = document.createElement('div');\n" +
                "      notif.className = 'notification';\n" +
                "      notif.textContent = message;\n" +
                "      document.body.appendChild(notif);\n" +
                "      setTimeout(() => notif.remove(), 3000);\n" +
                "    }\n" +
                "    \n" +
                "    // Initialize\n" +
                "    loadDashboard();\n" +
                "    loadMissions();\n" +
                "    \n" +
                "    // Auto-refresh every 30 seconds\n" +
                "    setInterval(loadDashboard, 30000);\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(html);
    }

    /**
     * San Diego 3D Map page — shows buildings, transit routes, and terrain
     * between Younger Industries HQ (4906 Campo Road, Spring Valley) and
     * the San Diego Convention Center.
     */
    @GetMapping(value = "/san-diego-map/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> sanDiegoMapPage() {
        String html = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>San Diego 3D Map - Earth 2026</title>\n" +
                "  <script src=\"https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js\"></script>\n" +
                "  <style>\n" +
                "    * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "    body { font-family: 'Segoe UI', Arial, sans-serif; background: #0a0e27; color: #fff; overflow: hidden; }\n" +
                "    #map-container { width: 100vw; height: 100vh; position: relative; }\n" +
                "    #map-canvas { width: 100%; height: 100%; display: block; }\n" +
                "    .hud-panel {\n" +
                "      position: absolute; background: rgba(10, 14, 39, 0.92); border: 1px solid rgba(0, 176, 255, 0.3);\n" +
                "      border-radius: 8px; padding: 16px; backdrop-filter: blur(10px);\n" +
                "    }\n" +
                "    .title-bar {\n" +
                "      position: absolute; top: 0; left: 0; right: 0; height: 56px;\n" +
                "      background: linear-gradient(135deg, rgba(26, 35, 126, 0.95), rgba(13, 71, 161, 0.95));\n" +
                "      display: flex; align-items: center; padding: 0 20px; z-index: 100;\n" +
                "      border-bottom: 2px solid rgba(0, 176, 255, 0.5);\n" +
                "    }\n" +
                "    .title-bar h1 { font-size: 20px; margin-right: 20px; }\n" +
                "    .title-bar .subtitle { font-size: 13px; opacity: 0.8; }\n" +
                "    .back-btn {\n" +
                "      background: rgba(0, 176, 255, 0.2); border: 1px solid rgba(0, 176, 255, 0.5);\n" +
                "      color: #fff; padding: 6px 16px; border-radius: 4px; cursor: pointer; font-size: 13px;\n" +
                "    }\n" +
                "    .back-btn:hover { background: rgba(0, 176, 255, 0.4); }\n" +
                "    #info-panel {\n" +
                "      top: 70px; left: 16px; width: 320px; max-height: calc(100vh - 90px);\n" +
                "      overflow-y: auto; z-index: 50;\n" +
                "    }\n" +
                "    #route-panel {\n" +
                "      top: 70px; right: 16px; width: 340px; max-height: calc(100vh - 90px);\n" +
                "      overflow-y: auto; z-index: 50;\n" +
                "    }\n" +
                "    #building-info {\n" +
                "      bottom: 16px; left: 50%; transform: translateX(-50%);\n" +
                "      width: 500px; display: none; z-index: 50;\n" +
                "      text-align: center;\n" +
                "    }\n" +
                "    .section-title {\n" +
                "      font-size: 14px; font-weight: 600; color: #00b4ff;\n" +
                "      margin-bottom: 10px; padding-bottom: 6px; border-bottom: 1px solid rgba(0,176,255,0.2);\n" +
                "    }\n" +
                "    .building-item, .route-item {\n" +
                "      background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1);\n" +
                "      border-radius: 6px; padding: 10px; margin-bottom: 8px; cursor: pointer;\n" +
                "      transition: all 0.2s;\n" +
                "    }\n" +
                "    .building-item:hover, .route-item:hover {\n" +
                "      background: rgba(0, 176, 255, 0.15); border-color: rgba(0, 176, 255, 0.4);\n" +
                "    }\n" +
                "    .building-item.active, .route-item.active {\n" +
                "      background: rgba(0, 176, 255, 0.25); border-color: #00b4ff;\n" +
                "    }\n" +
                "    .building-name { font-size: 13px; font-weight: 600; margin-bottom: 4px; }\n" +
                "    .building-address { font-size: 11px; opacity: 0.7; }\n" +
                "    .building-type {\n" +
                "      display: inline-block; font-size: 10px; padding: 2px 6px;\n" +
                "      border-radius: 3px; margin-top: 4px;\n" +
                "    }\n" +
                "    .type-corporate { background: #1a237e; }\n" +
                "    .type-landmark { background: #0d47a1; }\n" +
                "    .type-transit { background: #2e7d32; }\n" +
                "    .type-commercial { background: #4527a0; }\n" +
                "    .type-park { background: #1b5e20; }\n" +
                "    .type-stadium { background: #bf360c; }\n" +
                "    .type-nature { background: #33691e; }\n" +
                "    .route-type-badge {\n" +
                "      display: inline-block; font-size: 10px; padding: 2px 8px;\n" +
                "      border-radius: 3px; font-weight: 600; margin-right: 6px;\n" +
                "    }\n" +
                "    .badge-bus { background: #e65100; }\n" +
                "    .badge-trolley { background: #1565c0; }\n" +
                "    .route-name { font-size: 13px; font-weight: 600; }\n" +
                "    .route-detail { font-size: 11px; opacity: 0.7; margin-top: 4px; }\n" +
                "    .route-stops { font-size: 10px; opacity: 0.6; margin-top: 6px; }\n" +
                "    .legend {\n" +
                "      position: absolute; bottom: 16px; right: 16px; z-index: 50;\n" +
                "      background: rgba(10, 14, 39, 0.92); border: 1px solid rgba(0, 176, 255, 0.3);\n" +
                "      border-radius: 8px; padding: 12px; font-size: 11px;\n" +
                "    }\n" +
                "    .legend-item { display: flex; align-items: center; margin-bottom: 4px; }\n" +
                "    .legend-color { width: 12px; height: 12px; border-radius: 2px; margin-right: 8px; }\n" +
                "    .controls {\n" +
                "      position: absolute; bottom: 16px; left: 16px; z-index: 50;\n" +
                "      display: flex; flex-direction: column; gap: 6px;\n" +
                "    }\n" +
                "    .control-btn {\n" +
                "      background: rgba(10, 14, 39, 0.92); border: 1px solid rgba(0, 176, 255, 0.3);\n" +
                "      color: #fff; width: 36px; height: 36px; border-radius: 6px;\n" +
                "      cursor: pointer; font-size: 18px; display: flex; align-items: center; justify-content: center;\n" +
                "    }\n" +
                "    .control-btn:hover { background: rgba(0, 176, 255, 0.3); }\n" +
                "    #loading {\n" +
                "      position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);\n" +
                "      font-size: 18px; color: #00b4ff; z-index: 200;\n" +
                "    }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div id=\"map-container\">\n" +
                "    <canvas id=\"map-canvas\"></canvas>\n" +
                "    <div id=\"loading\">Loading San Diego 3D Map...</div>\n" +
                "\n" +
                "    <div class=\"title-bar\">\n" +
                "      <button class=\"back-btn\" onclick=\"window.location.href='/earth2026/'\">← Back to Game</button>\n" +
                "      <h1 style=\"margin-left:16px;\">San Diego 3D Map</h1>\n" +
                "      <span class=\"subtitle\">Younger Industries HQ → Convention Center</span>\n" +
                "    </div>\n" +
                "\n" +
                "    <div id=\"info-panel\" class=\"hud-panel\">\n" +
                "      <div class=\"section-title\">🏢 Buildings &amp; Locations</div>\n" +
                "      <div id=\"building-list\"></div>\n" +
                "    </div>\n" +
                "\n" +
                "    <div id=\"route-panel\" class=\"hud-panel\">\n" +
                "      <div class=\"section-title\">🚌 Transit Routes</div>\n" +
                "      <div id=\"route-list\"></div>\n" +
                "      <div id=\"path-info\" style=\"margin-top:12px;\"></div>\n" +
                "    </div>\n" +
                "\n" +
                "    <div id=\"building-info\" class=\"hud-panel\">\n" +
                "      <div id=\"building-info-content\"></div>\n" +
                "    </div>\n" +
                "\n" +
                "    <div class=\"legend\">\n" +
                "      <div class=\"section-title\">Legend</div>\n" +
                "      <div class=\"legend-item\"><div class=\"legend-color\" style=\"background:#1a237e\"></div>Corporate</div>\n" +
                "      <div class=\"legend-item\"><div class=\"legend-color\" style=\"background:#0d47a1\"></div>Landmark</div>\n" +
                "      <div class=\"legend-item\"><div class=\"legend-color\" style=\"background:#2e7d32\"></div>Transit</div>\n" +
                "      <div class=\"legend-item\"><div class=\"legend-color\" style=\"background:#4527a0\"></div>Commercial</div>\n" +
                "      <div class=\"legend-item\"><div class=\"legend-color\" style=\"background:#1b5e20\"></div>Park</div>\n" +
                "      <div class=\"legend-item\"><div class=\"legend-color\" style=\"background:#bf360c\"></div>Stadium</div>\n" +
                "      <div class=\"legend-item\"><div class=\"legend-color\" style=\"background:#e65100\"></div>Bus Route</div>\n" +
                "      <div class=\"legend-item\"><div class=\"legend-color\" style=\"background:#1565c0\"></div>Trolley Route</div>\n" +
                "    </div>\n" +
                "\n" +
                "    <div class=\"controls\">\n" +
                "      <button class=\"control-btn\" onclick=\"zoomIn()\" title=\"Zoom In\">+</button>\n" +
                "      <button class=\"control-btn\" onclick=\"zoomOut()\" title=\"Zoom Out\">−</button>\n" +
                "      <button class=\"control-btn\" onclick=\"resetView()\" title=\"Reset View\">⌂</button>\n" +
                "      <button class=\"control-btn\" onclick=\"toggleRoutes()\" title=\"Toggle Routes\">🚌</button>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  <script>\n" +
                "    // Three.js scene setup\n" +
                "    let scene, camera, renderer, controls;\n" +
                "    let buildings = [];\n" +
                "    let routes = [];\n" +
                "    let routeLines = [];\n" +
                "    let showRoutes = true;\n" +
                "    let selectedBuilding = null;\n" +
                "    let raycaster, mouse;\n" +
                "\n" +
                "    // Coordinate conversion: lat/lon to 3D world coordinates\n" +
                "    const CENTER_LAT = 32.8214;\n" +
                "    const CENTER_LON = -117.1000;\n" +
                "    const SCALE = 50000; // meters per degree\n" +
                "\n" +
                "    function latLonToWorld(lat, lon) {\n" +
                "      const x = (lon - CENTER_LON) * SCALE;\n" +
                "      const z = -(lat - CENTER_LAT) * SCALE;\n" +
                "      return { x: x, z: z };\n" +
                "    }\n" +
                "\n" +
                "    function init() {\n" +
                "      // Scene\n" +
                "      scene = new THREE.Scene();\n" +
                "      scene.background = new THREE.Color(0x0a0e27);\n" +
                "      scene.fog = new THREE.Fog(0x0a0e27, 500, 1500);\n" +
                "\n" +
                "      // Camera\n" +
                "      camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 1, 2000);\n" +
                "      camera.position.set(0, 200, 300);\n" +
                "      camera.lookAt(0, 0, 0);\n" +
                "\n" +
                "      // Renderer\n" +
                "      const canvas = document.getElementById('map-canvas');\n" +
                "      renderer = new THREE.WebGLRenderer({ canvas: canvas, antialias: true });\n" +
                "      renderer.setSize(window.innerWidth, window.innerHeight);\n" +
                "      renderer.setPixelRatio(window.devicePixelRatio);\n" +
                "\n" +
                "      // Lights\n" +
                "      const ambientLight = new THREE.AmbientLight(0x404060, 0.6);\n" +
                "      scene.add(ambientLight);\n" +
                "      const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);\n" +
                "      directionalLight.position.set(100, 200, 100);\n" +
                "      scene.add(directionalLight);\n" +
                "\n" +
                "      // Ground plane\n" +
                "      const groundGeometry = new THREE.PlaneGeometry(2000, 2000);\n" +
                "      const groundMaterial = new THREE.MeshLambertMaterial({ color: 0x1a1a2e });\n" +
                "      const ground = new THREE.Mesh(groundGeometry, groundMaterial);\n" +
                "      ground.rotation.x = -Math.PI / 2;\n" +
                "      ground.position.y = -1;\n" +
                "      scene.add(ground);\n" +
                "\n" +
                "      // Grid helper\n" +
                "      const grid = new THREE.GridHelper(2000, 40, 0x003366, 0x001a33);\n" +
                "      scene.add(grid);\n" +
                "\n" +
                "      // Raycaster for clicking buildings\n" +
                "      raycaster = new THREE.Raycaster();\n" +
                "      mouse = new THREE.Vector2();\n" +
                "\n" +
                "      // Simple orbit controls\n" +
                "      setupControls();\n" +
                "\n" +
                "      // Load map data\n" +
                "      loadMapData();\n" +
                "\n" +
                "      // Handle resize\n" +
                "      window.addEventListener('resize', onWindowResize);\n" +
                "      canvas.addEventListener('click', onCanvasClick);\n" +
                "\n" +
                "      // Hide loading\n" +
                "      document.getElementById('loading').style.display = 'none';\n" +
                "\n" +
                "      // Start animation\n" +
                "      animate();\n" +
                "    }\n" +
                "\n" +
                "    function setupControls() {\n" +
                "      const canvas = document.getElementById('map-canvas');\n" +
                "      let isDragging = false;\n" +
                "      let previousMousePosition = { x: 0, y: 0 };\n" +
                "      let cameraAngle = { theta: 0.3, phi: 1.0 };\n" +
                "      let cameraDistance = 350;\n" +
                "      let target = new THREE.Vector3(0, 0, 0);\n" +
                "\n" +
                "      canvas.addEventListener('mousedown', (e) => {\n" +
                "        isDragging = true;\n" +
                "        previousMousePosition = { x: e.clientX, y: e.clientY };\n" +
                "      });\n" +
                "\n" +
                "      canvas.addEventListener('mousemove', (e) => {\n" +
                "        if (!isDragging) return;\n" +
                "        const deltaMove = {\n" +
                "          x: e.clientX - previousMousePosition.x,\n" +
                "          y: e.clientY - previousMousePosition.y\n" +
                "        };\n" +
                "        cameraAngle.theta -= deltaMove.x * 0.005;\n" +
                "        cameraAngle.phi = Math.max(0.2, Math.min(Math.PI / 2 - 0.05, cameraAngle.phi - deltaMove.y * 0.005));\n" +
                "        previousMousePosition = { x: e.clientX, y: e.clientY };\n" +
                "        updateCamera(cameraAngle, cameraDistance, target);\n" +
                "      });\n" +
                "\n" +
                "      canvas.addEventListener('mouseup', () => isDragging = false);\n" +
                "      canvas.addEventListener('mouseleave', () => isDragging = false);\n" +
                "\n" +
                "      canvas.addEventListener('wheel', (e) => {\n" +
                "        cameraDistance = Math.max(50, Math.min(800, cameraDistance + e.deltaY * 0.5));\n" +
                "        updateCamera(cameraAngle, cameraDistance, target);\n" +
                "      });\n" +
                "\n" +
                "      function updateCamera(angle, distance, tgt) {\n" +
                "        camera.position.x = tgt.x + distance * Math.sin(angle.phi) * Math.sin(angle.theta);\n" +
                "        camera.position.y = tgt.y + distance * Math.cos(angle.phi);\n" +
                "        camera.position.z = tgt.z + distance * Math.sin(angle.phi) * Math.cos(angle.theta);\n" +
                "        camera.lookAt(tgt);\n" +
                "      }\n" +
                "\n" +
                "      // Expose zoom functions\n" +
                "      window.zoomIn = () => {\n" +
                "        cameraDistance = Math.max(50, cameraDistance - 50);\n" +
                "        updateCamera(cameraAngle, cameraDistance, target);\n" +
                "      };\n" +
                "      window.zoomOut = () => {\n" +
                "        cameraDistance = Math.min(800, cameraDistance + 50);\n" +
                "        updateCamera(cameraAngle, cameraDistance, target);\n" +
                "      };\n" +
                "      window.resetView = () => {\n" +
                "        cameraAngle = { theta: 0.3, phi: 1.0 };\n" +
                "        cameraDistance = 350;\n" +
                "        target = new THREE.Vector3(0, 0, 0);\n" +
                "        updateCamera(cameraAngle, cameraDistance, target);\n" +
                "      };\n" +
                "    }\n" +
                "\n" +
                "    async function loadMapData() {\n" +
                "      try {\n" +
                "        const resp = await fetch('/api/v1/san-diego-map/data');\n" +
                "        const data = await resp.json();\n" +
                "        renderBuildings(data.buildings || []);\n" +
                "        renderRoutes(data.routes || []);\n" +
                "        renderTerrain(data.terrain || {});\n" +
                "        renderBuildingList(data.buildings || []);\n" +
                "        renderRouteList(data.routes || []);\n" +
                "        renderPathInfo(data.hqToConventionCenter || {});\n" +
                "      } catch (e) {\n" +
                "        console.error('Failed to load map data:', e);\n" +
                "        document.getElementById('loading').textContent = 'Failed to load map data';\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    function renderBuildings(buildingsData) {\n" +
                "      buildingsData.forEach(b => {\n" +
                "        const pos = latLonToWorld(b.latitude, b.longitude);\n" +
                "        const geometry = new THREE.BoxGeometry(b.width, b.height, b.depth);\n" +
                "        const color = new THREE.Color(b.color);\n" +
                "        const material = new THREE.MeshPhongMaterial({\n" +
                "          color: color,\n" +
                "          transparent: true,\n" +
                "          opacity: 0.85,\n" +
                "          emissive: color.clone().multiplyScalar(0.2)\n" +
                "        });\n" +
                "        const mesh = new THREE.Mesh(geometry, material);\n" +
                "        mesh.position.set(pos.x, b.height / 2, pos.z);\n" +
                "        mesh.userData = { name: b.name, address: b.address, type: b.type, description: b.description };\n" +
                "        scene.add(mesh);\n" +
                "        buildings.push(mesh);\n" +
                "\n" +
                "        // Label\n" +
                "        const sprite = createTextSprite(b.name);\n" +
                "        sprite.position.set(pos.x, b.height + 10, pos.z);\n" +
                "        scene.add(sprite);\n" +
                "      });\n" +
                "    }\n" +
                "\n" +
                "    function createTextSprite(text) {\n" +
                "      const canvas = document.createElement('canvas');\n" +
                "      const ctx = canvas.getContext('2d');\n" +
                "      canvas.width = 256;\n" +
                "      canvas.height = 64;\n" +
                "      ctx.fillStyle = 'rgba(0, 0, 0, 0.6)';\n" +
                "      ctx.fillRect(0, 0, 256, 64);\n" +
                "      ctx.font = 'Bold 20px Arial';\n" +
                "      ctx.fillStyle = '#ffffff';\n" +
                "      ctx.textAlign = 'center';\n" +
                "      ctx.textBaseline = 'middle';\n" +
                "      ctx.fillText(text, 128, 32);\n" +
                "      const texture = new THREE.CanvasTexture(canvas);\n" +
                "      const material = new THREE.SpriteMaterial({ map: texture });\n" +
                "      const sprite = new THREE.Sprite(material);\n" +
                "      sprite.scale.set(60, 15, 1);\n" +
                "      return sprite;\n" +
                "    }\n" +
                "\n" +
                "    function renderRoutes(routesData) {\n" +
                "      routesData.forEach(route => {\n" +
                "        if (!route.stopsJson) return;\n" +
                "        try {\n" +
                "          const stops = JSON.parse(route.stopsJson);\n" +
                "          if (stops.length < 2) return;\n" +
                "\n" +
                "          const points = [];\n" +
                "          stops.forEach(stop => {\n" +
                "            const pos = latLonToWorld(stop[0], stop[1]);\n" +
                "            points.push(new THREE.Vector3(pos.x, 2, pos.z));\n" +
                "          });\n" +
                "\n" +
                "          const geometry = new THREE.BufferGeometry().setFromPoints(points);\n" +
                "          const color = route.routeType === 'Bus' ? 0xe65100 : 0x1565c0;\n" +
                "          const material = new THREE.LineBasicMaterial({\n" +
                "            color: color,\n" +
                "            linewidth: 3,\n" +
                "            transparent: true,\n" +
                "            opacity: 0.8\n" +
                "          });\n" +
                "          const line = new THREE.Line(geometry, material);\n" +
                "          line.userData = { routeType: route.routeType };\n" +
                "          scene.add(line);\n" +
                "          routeLines.push(line);\n" +
                "\n" +
                "          // Stop markers\n" +
                "          points.forEach((p, i) => {\n" +
                "            const sphereGeo = new THREE.SphereGeometry(3, 8, 8);\n" +
                "            const sphereMat = new THREE.MeshPhongMaterial({ color: color, emissive: color });\n" +
                "            const sphere = new THREE.Mesh(sphereGeo, sphereMat);\n" +
                "            sphere.position.copy(p);\n" +
                "            sphere.position.y = 3;\n" +
                "            scene.add(sphere);\n" +
                "          });\n" +
                "        } catch (e) {\n" +
                "          console.error('Failed to parse route stops:', e);\n" +
                "        }\n" +
                "      });\n" +
                "    }\n" +
                "\n" +
                "    function renderTerrain(terrainData) {\n" +
                "      const features = terrainData.features || [];\n" +
                "      features.forEach(feature => {\n" +
                "        const pos = latLonToWorld(feature.centerLat, feature.centerLon);\n" +
                "        let geometry, material;\n" +
                "\n" +
                "        if (feature.type === 'Water' || feature.type === 'Ocean') {\n" +
                "          geometry = new THREE.CircleGeometry(feature.radius / SCALE * 50, 32);\n" +
                "          material = new THREE.MeshLambertMaterial({\n" +
                "            color: feature.color,\n" +
                "            transparent: true,\n" +
                "            opacity: 0.5\n" +
                "          });\n" +
                "          const mesh = new THREE.Mesh(geometry, material);\n" +
                "          mesh.rotation.x = -Math.PI / 2;\n" +
                "          mesh.position.set(pos.x, 0.5, pos.z);\n" +
                "          scene.add(mesh);\n" +
                "        } else if (feature.type === 'Hills') {\n" +
                "          geometry = new THREE.ConeGeometry(feature.radius / SCALE * 30, feature.elevation / 5, 8);\n" +
                "          material = new THREE.MeshLambertMaterial({ color: feature.color, transparent: true, opacity: 0.4 });\n" +
                "          const mesh = new THREE.Mesh(geometry, material);\n" +
                "          mesh.position.set(pos.x, feature.elevation / 10, pos.z);\n" +
                "          scene.add(mesh);\n" +
                "        } else if (feature.type === 'Urban') {\n" +
                "          geometry = new THREE.PlaneGeometry(feature.radius / SCALE * 60, feature.radius / SCALE * 60);\n" +
                "          material = new THREE.MeshLambertMaterial({ color: feature.color, transparent: true, opacity: 0.3 });\n" +
                "          const mesh = new THREE.Mesh(geometry, material);\n" +
                "          mesh.rotation.x = -Math.PI / 2;\n" +
                "          mesh.position.set(pos.x, 0.3, pos.z);\n" +
                "          scene.add(mesh);\n" +
                "        }\n" +
                "      });\n" +
                "    }\n" +
                "\n" +
                "    function renderBuildingList(buildingsData) {\n" +
                "      const list = document.getElementById('building-list');\n" +
                "      list.innerHTML = '';\n" +
                "      buildingsData.forEach((b, i) => {\n" +
                "        const item = document.createElement('div');\n" +
                "        item.className = 'building-item';\n" +
                "        item.innerHTML = `\n" +
                "          <div class=\"building-name\">${b.name}</div>\n" +
                "          <div class=\"building-address\">${b.address}</div>\n" +
                "          <span class=\"building-type type-${b.type.toLowerCase()}\">${b.type}</span>\n" +
                "        `;\n" +
                "        item.onclick = () => focusOnBuilding(i);\n" +
                "        list.appendChild(item);\n" +
                "      });\n" +
                "    }\n" +
                "\n" +
                "    function renderRouteList(routesData) {\n" +
                "      const list = document.getElementById('route-list');\n" +
                "      list.innerHTML = '';\n" +
                "      routesData.forEach(route => {\n" +
                "        const item = document.createElement('div');\n" +
                "        item.className = 'route-item';\n" +
                "        const badgeClass = route.routeType === 'Bus' ? 'badge-bus' : 'badge-trolley';\n" +
                "        item.innerHTML = `\n" +
                "          <span class=\"route-type-badge ${badgeClass}\">${route.routeType}</span>\n" +
                "          <span class=\"route-name\">${route.routeName}</span>\n" +
                "          <div class=\"route-detail\">${route.startLocation} → ${route.endLocation}</div>\n" +
                "          <div class=\"route-detail\">⏱ ${route.travelTimeMinutes} min | 💰 $${(route.fareCents / 100).toFixed(2)}</div>\n" +
                "          <div class=\"route-stops\">Stops: ${route.stopNames ? route.stopNames.length : 0}</div>\n" +
                "        `;\n" +
                "        list.appendChild(item);\n" +
                "      });\n" +
                "    }\n" +
                "\n" +
                "    function renderPathInfo(pathData) {\n" +
                "      const info = document.getElementById('path-info');\n" +
                "      if (!pathData.segments || pathData.segments.length === 0) {\n" +
                "        info.innerHTML = '';\n" +
                "        return;\n" +
                "      }\n" +
                "      let html = '<div class=\"section-title\">📍 HQ → Convention Center Path</div>';\n" +
                "      html += `<div style=\"font-size:11px;opacity:0.7;margin-bottom:8px;\">${pathData.description || ''}</div>`;\n" +
                "      pathData.segments.forEach(seg => {\n" +
                "        const badgeClass = seg.type === 'Bus' ? 'badge-bus' : 'badge-trolley';\n" +
                "        html += `<div style=\"margin-bottom:8px;padding:8px;background:rgba(255,255,255,0.05);border-radius:6px;\">`;\n" +
                "        html += `<span class=\"route-type-badge ${badgeClass}\">${seg.type}</span> <strong>${seg.route}</strong><br>`;\n" +
                "        html += `<div style=\"font-size:10px;opacity:0.7;\">${seg.from}</div>`;\n" +
                "        html += `<div style=\"font-size:10px;opacity:0.7;\">↓ ${seg.duration}, ${seg.fare}</div>`;\n" +
                "        html += `<div style=\"font-size:10px;opacity:0.7;\">${seg.to}</div>`;\n" +
                "        html += '</div>';\n" +
                "      });\n" +
                "      html += `<div style=\"font-size:12px;font-weight:600;margin-top:8px;\">Total: ${pathData.totalDuration || ''} | ${pathData.totalFare || ''}</div>`;\n" +
                "      if (pathData.transferAt) {\n" +
                "        html += `<div style=\"font-size:10px;opacity:0.6;\">Transfer at: ${pathData.transferAt}</div>`;\n" +
                "      }\n" +
                "      info.innerHTML = html;\n" +
                "    }\n" +
                "\n" +
                "    function focusOnBuilding(index) {\n" +
                "      if (index < 0 || index >= buildings.length) return;\n" +
                "      const building = buildings[index];\n" +
                "      selectedBuilding = building;\n" +
                "\n" +
                "      // Highlight\n" +
                "      buildings.forEach(b => b.material.emissive.setHex(0x000000));\n" +
                "      building.material.emissive.setHex(0x00b4ff);\n" +
                "\n" +
                "      // Show info\n" +
                "      const infoPanel = document.getElementById('building-info');\n" +
                "      const infoContent = document.getElementById('building-info-content');\n" +
                "      infoPanel.style.display = 'block';\n" +
                "      infoContent.innerHTML = `\n" +
                "        <h3 style=\"margin-bottom:8px;\">${building.userData.name}</h3>\n" +
                "        <div style=\"font-size:12px;opacity:0.8;margin-bottom:6px;\">${building.userData.address}</div>\n" +
                "        <div style=\"font-size:11px;opacity:0.7;\">${building.userData.description || ''}</div>\n" +
                "      `;\n" +
                "\n" +
                "      // Highlight in list\n" +
                "      document.querySelectorAll('.building-item').forEach((el, i) => {\n" +
                "        el.classList.toggle('active', i === index);\n" +
                "      });\n" +
                "    }\n" +
                "\n" +
                "    function onCanvasClick(event) {\n" +
                "      mouse.x = (event.clientX / window.innerWidth) * 2 - 1;\n" +
                "      mouse.y = -(event.clientY / window.innerHeight) * 2 + 1;\n" +
                "      raycaster.setFromCamera(mouse, camera);\n" +
                "      const intersects = raycaster.intersectObjects(buildings);\n" +
                "      if (intersects.length > 0) {\n" +
                "        const index = buildings.indexOf(intersects[0].object);\n" +
                "        if (index >= 0) focusOnBuilding(index);\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    function toggleRoutes() {\n" +
                "      showRoutes = !showRoutes;\n" +
                "      routeLines.forEach(line => line.visible = showRoutes);\n" +
                "    }\n" +
                "    window.toggleRoutes = toggleRoutes;\n" +
                "\n" +
                "    function onWindowResize() {\n" +
                "      camera.aspect = window.innerWidth / window.innerHeight;\n" +
                "      camera.updateProjectionMatrix();\n" +
                "      renderer.setSize(window.innerWidth, window.innerHeight);\n" +
                "    }\n" +
                "\n" +
                "    function animate() {\n" +
                "      requestAnimationFrame(animate);\n" +
                "      renderer.render(scene, camera);\n" +
                "    }\n" +
                "\n" +
                "    // Initialize\n" +
                "    init();\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(html);
    }

    // Add "3D Map" button to Earth 2026 game page mission list actions
}
