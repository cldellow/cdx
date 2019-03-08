# html-locations
Given an HTML page, try to extract a latitude and longitude from it.

## Extracting from GeoNames

1. Download http://download.geonames.org/export/dump/allCountries.zip
2. Run `<allCountries.txt awk -F $'\t' 'BEGIN { OFS = "\t" } ($9 == "CA" || $9 == "GB" || $9 == "US" || $9 == "IE" || $9 == "AU" || $9 == "NZ") && $7 == "P"' { print $5, $6, $2, $11, $9 }` > cities.tsv

This creates `cities.tsv` with these columns: `latitude`, `longitude`, `city name`, `state code`, `country ISO-3166-2 code`.
