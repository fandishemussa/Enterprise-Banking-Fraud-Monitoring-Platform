import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export function CustomerDevicesTable({ devices }: { devices: string[] }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Devices Used</CardTitle>
      </CardHeader>
      <CardContent>
        {devices.length === 0 ? (
          <p className="text-sm text-muted-foreground">No devices recorded.</p>
        ) : (
          <ul className="space-y-1 text-sm">
            {devices.map((device) => (
              <li key={device} className="rounded-md bg-muted px-2 py-1 font-mono text-xs">
                {device}
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  );
}
