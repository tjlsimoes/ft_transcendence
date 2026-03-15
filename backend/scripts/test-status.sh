#!/usr/bin/env bash
set -euo pipefail

./mvnw -q test >/tmp/mvn-test.log 2>&1

perl -0777 -ne '
while (/<testcase\b([^>]*)\/>|<testcase\b([^>]*)>(.*?)<\/testcase>/sg) {
  $attrs = defined($1) ? $1 : $2;
  $body = defined($3) ? $3 : "";

  ($name) = $attrs =~ /name="([^"]+)"/;

  $status = $body =~ /<(failure|error)\b/ ? "❌" :
            $body =~ /<skipped\b/ ? "⏭️" : "✅";

  $ok++ if $status eq "✅";
  $fail++ if $status eq "❌";
  $skip++ if $status eq "⏭️";

  print "$status $name\n";
}

END {
  print "\nSummary: OK=$ok FAIL=$fail SKIP=$skip\n";
}
' target/surefire-reports/TEST-*.xml
