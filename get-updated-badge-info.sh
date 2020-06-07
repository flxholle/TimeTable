#!/bin/bash

echo "Collecting stats for badges..."

commits=$(git rev-list --all --count)

latest_release_tag=$(git describe --tags --always "$(git rev-list --tags --max-count=1)")
latest_release_timestamp=$(git log -1 --format=%ct "$latest_release_tag")

latest_release_date=$(date -d @"$latest_release_timestamp" +"%h %Y")
latest_release_date_layout2=$(date -d @"$latest_release_timestamp" +%d.%m.%Y)

authors=$(git shortlog -sne)
authorsCount=$(echo "$authors" | wc -l)

authorsAll=$(git shortlog -sne --all)
authorsAllCount=$(echo "$authorsAll" | wc -l)

first_commit_hash=$(git rev-list --max-parents=0 HEAD --max-count=1)
first_commit_timestamp=$(git show -s --format=%ct "$first_commit_hash")

commits_since_last_release_hashes=$(git rev-list "$latest_release_tag"..HEAD)
commits_since_last_release=$(echo "$commits_since_last_release_hashes" | sed '/^\s*$/d' | wc -l)

repository_creation_day_timestamp=$(git show -s --format=%ct "$first_commit_hash")
repository_creation_day=$(date -d @"$repository_creation_day_timestamp" +%d.%m.%Y)

difference_in_seconds=$(($(date +"%s") - first_commit_timestamp))
difference_in_minutes=$((difference_in_seconds / 60))
difference_in_hours=$((difference_in_minutes / 60))
difference_in_days=$((difference_in_hours / 24))
difference_in_months=$((difference_in_days / 30))
difference_in_years=$((difference_in_days / 365))

time_repository_exists="$difference_in_months months $((difference_in_days - (difference_in_months * 30))) days"
if [ "$difference_in_years" -gt "0" ]; then
  diff_months=$((difference_in_months - (difference_in_years * 12)))
  time_repository_exists="$difference_in_years years $diff_months months $((difference_in_days - (difference_in_months * 30))) days"
fi

if [ "$difference_in_minutes" -eq 0 ]; then
  difference_in_minutes=1
fi
if [ "$difference_in_hours" -eq 0 ]; then
  difference_in_hours=1
fi
if [ "$difference_in_days" -eq 0 ]; then
  difference_in_days=1
fi
if [ "$difference_in_months" -eq 0 ]; then
  difference_in_months=1
fi
if [ "$difference_in_years" -eq 0 ]; then
  difference_in_years=1
fi
commits_per_second=$((commits / difference_in_seconds))
commits_per_minute=$((commits / difference_in_minutes))
commits_per_hour=$((commits / difference_in_hours))
commits_per_day=$((commits / difference_in_days))
commits_per_month=$((commits / difference_in_months))
commit_activity="$commits_per_month/month"
commits_per_year=$((commits / difference_in_years))

releases_names=$(git tag)
releases_amount=$(echo "$releases_names" | sed '/^\s*$/d' | wc -l)

releases_per_second=$((releases_amount / difference_in_seconds))
releases_per_minute=$((releases_amount / difference_in_minutes))
releases_per_hour=$((releases_amount / difference_in_hours))
releases_per_day=$((releases_amount / difference_in_days))
releases_per_month=$((releases_amount / difference_in_months))
releases_activity="$releases_per_month/month"
releases_per_year=$((releases_amount / difference_in_years))

last_commit_hash=$(git rev-list HEAD^..HEAD --max-count=1)
last_commit_timestamp=$(git show -s --format=%ct "$last_commit_hash")

last_commit_date=$(date -d @"$last_commit_timestamp" +"%h %Y")
last_commit_date_layout2=$(date -d @"$last_commit_timestamp" +%d.%m.%Y)

git gc -q
git_repository_size=$(du -sh)
git_repository_size=$(echo "$git_repository_size" | xargs)
#git_repository_size=${git_repository_size//[[:blank:]]/} || echo "$git_repository_size"
#git_repository_size=${git_repository_size//" ."/} || echo "$git_repository_size"
git_file_size=$(du -sh .git/)
git_file_size=$(echo "$git_file_size" | xargs)
#git_file_size=${git_file_size//[[:blank:]]/} || echo "$git_file_size"
#git_file_size=${git_file_size//" .git/"/} || echo "$git_file_size"

echo "{\"commits\":\"$commits\", \"release_tag\":\"$latest_release_tag\", \"releases_amount\":\"$releases_amount\", \"contributors\":\"$authorsCount\", \"all_contributors\":\"$authorsAllCount\", \"commits_per_second\":\"$commits_per_second\", \"commits_per_minute\":\"$commits_per_minute\", \"commits_per_hour\":\"$commits_per_hour\",\"commits_per_day\":\"$commits_per_day\", \"commits_per_month\":\"$commits_per_month\", \"commits_per_year\":\"$commits_per_year\",\"commit_activity\":\"$commit_activity\",\"time_repository_exists\":\"$time_repository_exists\", \"repository_creation_day\":\"$repository_creation_day\",\"commits_since_last_release\":\"$commits_since_last_release\",\"last_commit_date\":\"$last_commit_date\",\"last_commit_date_layout2\":\"$last_commit_date_layout2\", \"last_release_date\":\"$latest_release_date\",\"last_release_date_layout2\":\"$latest_release_date_layout2\",\"repository_size\":\"$git_repository_size\", \"repository_file_size\":\"$git_file_size\", \"releases_per_second\":\"$releases_per_second\", \"releases_per_minute\":\"$releases_per_minute\", \"releases_per_hour\":\"$releases_per_hour\",\"releases_per_day\":\"$releases_per_day\", \"releases_per_month\":\"$releases_per_month\", \"releases_per_year\":\"$releases_per_year\",\"releases_activity\":\"$releases_activity\"}" >badges.json

echo "Generating anybadge badges..."

mkdir -p badges
rm -rf badges/*
anybadge --value="$commits" --label="Commits" --color=red --file=badges/commits.svg
anybadge --value="$latest_release_tag" --label="Release" --color=green --file=badges/latest_release.svg
anybadge --value="$latest_release_date" --label="Released $latest_release_tag in" --color=green --file=badges/latest_release_date.svg
anybadge --value="$latest_release_date_layout2" --label="Released $latest_release_tag on" --color=green --file=badges/latest_release_date_layout2.svg
anybadge --value="$authorsAllCount" --label="All contributors" --color=#0B7CBC --file=badges/all_contributors.svg
anybadge --value="$authorsCount" --label="Contributors" --color=#0B7CBC --file=badges/contributors.svg
anybadge --value="$commits_since_last_release" --label="Commits since $latest_release_tag" --color=purple --file=badges/commits_since_last_release.svg
anybadge --value="$repository_creation_day" --label="Created on" --color=teal --file=badges/repository_creation_day.svg
anybadge --value="$time_repository_exists" --label="The repository exists" --color=#89B702 --file=badges/time_repository_exists.svg
anybadge --value="$commits_per_second" --label="Commits per second" --color=#0B7CBC --file=badges/commits_per_second.svg
anybadge --value="$commits_per_minute" --label="Commits per minute" --color=#0B7CBC --file=badges/commits_per_minute.svg
anybadge --value="$commits_per_hour" --label="Commits per hour" --color=#0B7CBC --file=badges/commits_per_hour.svg
anybadge --value="$commits_per_day" --label="Commits per day" --color=#0B7CBC --file=badges/commits_per_day.svg
anybadge --value="$commits_per_month" --label="Commits per month" --color=yellow --file=badges/commits_per_month.svg
anybadge --value="$commits_per_year" --label="Commits per year" --color=yellowgreen --file=badges/commits_per_year.svg
anybadge --value="$commit_activity" --label="Commit activity" --color=orange --file=badges/commit_activity.svg
anybadge --value="$last_commit_date" --label="Last commit" --color=red --file=badges/last_commit_date.svg
anybadge --value="$last_commit_date_layout2" --label="Last commit" --color=red --file=badges/last_commit_date_layout2.svg
anybadge --value="$git_repository_size" --label="Git repository size" --color=lightgrey --file=badges/git_repository_size.svg
anybadge --value="$git_file_size" --label="Git repository files size" --color=lightgrey --file=badges/git_file_size.svg
anybadge --value="$releases_per_second" --label="Releases per second" --color=#0B7CBC --file=badges/releases_per_second.svg
anybadge --value="$releases_per_minute" --label="Releases per minute" --color=#0B7CBC --file=badges/releases_per_minute.svg
anybadge --value="$releases_per_hour" --label="Releases per hour" --color=#0B7CBC --file=badges/releases_per_hour.svg
anybadge --value="$releases_per_day" --label="Releases per day" --color=#0B7CBC --file=badges/releases_per_day.svg
anybadge --value="$releases_per_month" --label="Releases per month" --color=yellow --file=badges/releases_per_month.svg
anybadge --value="$releases_per_year" --label="Releases per year" --color=yellowgreen --file=badges/releases_per_year.svg
anybadge --value="$releases_activity" --label="Release activity" --color=orange --file=badges/releases_activity.svg
anybadge --value="$releases_amount" --label="Releases" --color=maroon --file=badges/releases_amount.svg
