# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A novel reading miniapp ("书页间") with a uni-app Vue3 frontend (H5 target) and a Spring Boot Java backend. Core features: continuous-flow reader, ad-unlock chapters, bookshelf, reading progress, and coin-based purchases.

## Repository Structure

```
bookstore-miniapp/
├── uni/                    # Frontend: uni-app (Vue3 + TypeScript + Pinia)
│   ├── src/pages/          # Pages: index, store, shelf, me, detail, reader, recharge
│   ├── src/api/            # API modules per domain (book, user, comment, ad)
│   ├── src/store/          # Pinia stores (user.ts is the main one)
│   ├── src/utils/          # request.ts (wraps uni.request, attaches Bearer token), constants.ts
│   ├── src/components/     # Shared Vue components (BookCard, CustomTabBar, Terminal, etc.)
│   ├── src/static/         # Static assets including cover images
│   └── dist/build/h5/      # H5 build output (served by proxy.js)
├── java-server/            # Backend: Spring Boot 3.2.5 + Java 17 + PostgreSQL
│   └── src/main/java/com/bookstore/bookstore/
│       ├── controller/     # REST controllers (all under /api/*)
│       ├── service/        # Business logic
│       ├── repository/     # Spring Data JPA repositories
│       ├── entity/         # JPA entities (Book, Chapter, User, BookShelf, etc.)
│       ├── dto/            # Request/response DTOs
│       ├── security/       # JWT filter, token provider
│       └── config/         # SecurityConfig, JwtConfig
├── scripts/                # Python scraping & data import scripts
├── proxy.js                # Dev proxy: static H5 + API forward to :3001
└── pages/                  # Legacy WeChat mini-program pages (not the active target)
```

## Development Commands

### Frontend (uni/)

```bash
cd uni
npm install
npm run dev:h5        # H5 dev server
npm run build:h5      # H5 production build → dist/build/h5/
npm run type-check    # vue-tsc --noEmit
```

### Backend (java-server/)

```bash
cd java-server
mvn spring-boot:run   # Starts on :3001
```

Requires PostgreSQL running locally with database `bookstore`. Connection config is in `src/main/resources/application.yml` (username `zoulingxi`, empty password).

### Local Dev Proxy (root)

```bash
node proxy.js         # Serves H5 static files + proxies /api/* to :3001, runs on :8080
```

Development workflow: start PostgreSQL → `mvn spring-boot:run` → `npm run build:h5` → `node proxy.js` → open http://localhost:8080.

## Architecture Notes

### Frontend (uni-app → H5)

- **Target platform is H5**, not WeChat mini-program. The app uses a custom tab bar (`CustomTabBar.vue`) and hides the native uni-tabbar via CSS in `App.vue`.
- **No build-time base URL**: `uni/src/utils/request.ts` uses an empty `BASE_URL`, relying on same-origin requests. In development this means requests go to the proxy (:8080) which forwards `/api/*` to the Java backend (:3001).
- **Auth flow**: JWT token is stored in `uni.getStorageSync('token')` and attached to every request. On 401/403, the token is cleared and user is redirected to `/pages/me/me`.
- **H5 login mock**: Since WeChat `code2Session` doesn't work in H5, the auth controller falls back to IP-based openids (`h5_<clientIp>`) when the code starts with `h5_`.
- **State management**: Single Pinia store `useUserStore` in `store/user.ts` handles profile, login state, and coin balance. It persists to `uni.getStorageSync('user_profile')`.
- **Skeleton loading pattern**: Pages use `isLoading` + `firstLoad` flags to show skeleton UIs on first mount, then cache data for subsequent `onShow`.

### Backend (Spring Boot)

- **Layered architecture**: Controller → Service → Repository → Entity. No explicit DTO-to-entity mapping layer; controllers return entities directly (with `@JsonIgnore` to prevent cycles).
- **Security**: Spring Security with `JwtAuthFilter` (`security/JwtAuthFilter.java`). JWT secret and expiration are in `application.yml`. Most read endpoints are public; write endpoints require auth.
- **Database**: PostgreSQL with `ddl-auto: none` — schema is managed manually. Physical naming strategy is `PhysicalNamingStrategyStandardImpl` (preserves camelCase table/column names, but `globally_quoted_identifiers: true` quotes them for PostgreSQL compatibility).
- **CORS**: Configured to allow all origins (`*`) in `SecurityConfig`.
- **Admin endpoints**: `/api/admin/**` are permit-all (development convenience).

### Key Entities

- `Book` — novel metadata (title, author, category, tags, cover, price, status, rating)
- `Chapter` — belongs to Book, has `isFree`, `price`, `wordCount`
- `User` — openid-based, has `coins` and `vipStatus`
- `BookShelf` — user's saved books with last read chapter
- `ReadingRecord` — per-book progress (chapter + percentage)
- `UnlockedChapter` — chapters unlocked via coins or ads
- `AdToken` / `AdDailyLimit` — ad unlock tracking
- `ReaderConfig` — per-user reader settings (font size, theme, line spacing, brightness)
- `Order` — coin recharge orders

### Data Import

The `scripts/` directory contains Python scrapers (`scrape_*.py`) that fetch novels and chapters from external sites, plus `import_*.py` scripts to load them into PostgreSQL. These are one-off data seeding tools, not part of the runtime application.

## Important Files

- `uni/src/utils/request.ts` — All API calls go through here.
- `uni/src/App.vue` — Global styles, custom tab bar hiding, safe-area padding.
- `uni/src/pages.json` — Page registration and tab bar config.
- `java-server/src/main/resources/application.yml` — DB and JWT config.
- `java-server/.../config/SecurityConfig.java` — Public vs authenticated route definitions.
- `proxy.js` — Dev server routing logic.
