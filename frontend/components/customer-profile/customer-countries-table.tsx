import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export function CustomerCountriesTable({ countries }: { countries: string[] }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Countries Used</CardTitle>
      </CardHeader>
      <CardContent>
        {countries.length === 0 ? (
          <p className="text-sm text-muted-foreground">No countries recorded.</p>
        ) : (
          <ul className="space-y-1 text-sm">
            {countries.map((country) => (
              <li key={country} className="rounded-md bg-muted px-2 py-1 text-xs">
                {country}
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  );
}
