# Custom domain setup

Domain: `talmudfinance.com`

## GitHub Pages

Repository: `ainihonuragawa-lang/TalmudFinance`

1. Open repository Settings -> Pages.
2. Keep Source as `GitHub Actions`.
3. In Custom domain, enter `talmudfinance.com` and save.
4. After DNS is detected, enable `Enforce HTTPS`.

## XServer DNS records

Add these records for the apex domain.

| Type | Host | Value |
|---|---|---|
| A | @ | 185.199.108.153 |
| A | @ | 185.199.109.153 |
| A | @ | 185.199.110.153 |
| A | @ | 185.199.111.153 |

Optional but recommended for `www.talmudfinance.com`.

| Type | Host | Value |
|---|---|---|
| CNAME | www | ainihonuragawa-lang.github.io |

Do not add wildcard records such as `*.talmudfinance.com`.
DNS propagation can take time. GitHub documents that changes may take up to 24 hours.